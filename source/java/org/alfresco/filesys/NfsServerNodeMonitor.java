/*
 * Copyright (C) 2006-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.filesys;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.oncrpc.nfs.NFSServer;
import org.alfresco.jlan.oncrpc.nfs.ShareDetails;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Node monitor for NFS server which updates NFS cache on renaming or deleting nodes not through NFS protocol. This monitor may be dynamically enabled or disabled. It handles nodes
 * for <code>${filesystem.name}</code> device name
 * 
 * @author Dmitry Velichkevich
 */
public class NfsServerNodeMonitor
        implements
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy,
        NodeServicePolicies.OnDeleteNodePolicy,
        InitializingBean
{
    private static final Logger LOGGER = Logger.getLogger(NfsServerNodeMonitor.class);

    public static final char NIX_SEPARATOR = '/';
    public static final String NIX_SEPARATOR_STR = "/";

    // 
    // Not static fields (or bean properties)
    //

    private Boolean enabled;

    private String targetDeviceName;

    // Calculable value
    private StoreRef targetStoreRef;

    private List<DeviceContext> filesystemContexts;

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private PermissionService permissionService;

    /**
     * Calculable value (see {@link NFSServerBean})
     */
    private NFSServer nfsServer;

    private Map<NodeRef, Integer> cachedNodes = new HashMap<NodeRef, Integer>();

    public NfsServerNodeMonitor()
    {
    }

    /**
     * Enables or disables policy handlers
     * 
     * @param enabled {@link Boolean} value which determines working state of the handler
     */
    public void setEnabled(boolean enabled)
    {
        Object previousState = this.enabled;
        this.enabled = enabled;
        if (null != previousState)
        {
            initialize();
        }
    }

    public Boolean isEnabled()
    {
        return enabled;
    }

    public void setTargetDeviceName(String targetDeviceName)
    {
        this.targetDeviceName = targetDeviceName;
    }

    public String getTargetDeviceName()
    {
        return targetDeviceName;
    }

    public void setFilesystemContexts(List<DeviceContext> filesystemContexts)
    {
        this.filesystemContexts = filesystemContexts;
    }

    public StoreRef getTargetStoreRef()
    {
        return targetStoreRef;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setNfsServer(NFSServer nfsServer)
    {
        this.nfsServer = nfsServer;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        initialize();
    }

    /**
     * Performs all check on mandatory properties, searches for {@link StoreRef} for root path of target device and registers policy handlers for NFS cache updating on node
     * properties updating and node deleting
     */
    private void initialize()
    {
        if (enabled)
        {
            if (null == filesystemContexts)
            {
                throw new AlfrescoRuntimeException("'filesystemContexts' property is not configured");
            }
            for (DeviceContext context : filesystemContexts)
            {
                if ((context instanceof ContentContext) && (null != context.getDeviceName()) && context.getDeviceName().equals(targetDeviceName))
                {
                    ContentContext targetContext = (ContentContext) context;
                    if (null != targetContext.getStoreName())
                    {
                        targetStoreRef = new StoreRef(targetContext.getStoreName());
                    }
                    break;
                }
            }
            if (null == targetStoreRef)
            {
                throw new AlfrescoRuntimeException("Target Store Reference can't be found for '" + targetDeviceName
                        + "' device name. Check correctness of 'targetDeviceName' and 'filesystemContexts' properties configurations");
            }

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("StoreRef='" + targetStoreRef + "' was found for '" + targetDeviceName + "' device name");
            }

            policyComponent.bindClassBehaviour(OnDeleteNodePolicy.QNAME, this, new JavaBehaviour(this, "onDeleteNode"));
            policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, this, new JavaBehaviour(this, "beforeDeleteNode"));
            policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, this, new JavaBehaviour(this, "onUpdateProperties"));
        }
        else
        {
            LOGGER.warn("NodeMonitor for NFS server is not enabled! Cache of NFS server will be never synchronized with target filesystem");
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (enabled && (null != nfsServer) && targetStoreRef.equals(nodeRef.getStoreRef()))
        {
            int dbId = DefaultTypeConverter.INSTANCE.intValue(nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
            if (null == findShareDetailsForId(dbId))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Node with nodeRef='" + nodeRef + "' and dbId='" + dbId + "' is not in NFS server cache");
                }

                return;
            }
            cachedNodes.put(nodeRef, dbId);

            String oldName = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(ContentModel.PROP_NAME));
            String newName = DefaultTypeConverter.INSTANCE.convert(String.class, after.get(ContentModel.PROP_NAME));

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("oldName='" + oldName + "', newName='" + newName + "'");
            }

            if (((null == oldName) && (null != newName)) || ((null != oldName) && !oldName.equals(newName)))
            {
                String path = buildRelativePath(nodeRef, newName);

                updateNfsCache(nodeRef, path);
            }
        }
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (enabled && (null != nfsServer) && (null != nodeRef) && targetStoreRef.equals(nodeRef.getStoreRef()))
        {
            int dbId = DefaultTypeConverter.INSTANCE.intValue(nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
            cachedNodes.put(nodeRef, dbId);
        }
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        NodeRef nodeRef = (null != childAssocRef) ? (childAssocRef.getChildRef()) : (null);
        if (enabled && (null != nfsServer) && (null != nodeRef) && targetStoreRef.equals(nodeRef.getStoreRef()))
        {
            updateNfsCache(nodeRef, null);
        }
    }

    /**
     * Searches for {@link ShareDetails} to access NFS server cache for specific device name (e.g. 'Alfresco', 'AVM' etc)
     * 
     * @param fileId - {@link Integer} value which contains <code>fileId</code> specific to device
     * @return {@link ShareDetails} instance which contains <code>fileId</code> key in the cache or <code>null</code> if such instance was not found
     */
    private ShareDetails findShareDetailsForId(int fileId)
    {
        if ((null == nfsServer) || (null == nfsServer.getShareDetails()))
        {
            return null;
        }

        Hashtable<Integer, ShareDetails> details = nfsServer.getShareDetails().getShareDetails();
        for (Integer key : details.keySet())
        {
            ShareDetails shareDetails = details.get(key);
            if (null != shareDetails.getFileIdCache().findPath(fileId))
            {
                return shareDetails;
            }
        }

        return null;
    }

    /**
     * Builds path relative to NFS device name e.g. for the nfs.domain.name:/DeviceName/folder/document.doc method will return \folder\document.doc
     * 
     * @param nodeRef - {@link NodeRef} instance for target node
     * @param newName - {@link String} value which contains new name for the node
     * @return {@link String} value of relative path
     */
    private String buildRelativePath(NodeRef nodeRef, String newName)
    {
        String result = null;
        Path path = nodeService.getPath(nodeRef);
        if (null != path)
        {
            StringBuilder newPath = new StringBuilder(path.toDisplayPath(nodeService, permissionService));
            if ((NIX_SEPARATOR == newPath.charAt(0)) || (FileName.DOS_SEPERATOR == newPath.charAt(0)))
            {
                newPath.delete(0, 1);
            }
            int indexOfFirstSeparator = newPath.indexOf(NIX_SEPARATOR_STR);
            indexOfFirstSeparator = (-1 == indexOfFirstSeparator) ? (newPath.indexOf(FileName.DOS_SEPERATOR_STR)) : (indexOfFirstSeparator);
            char lastChar = newPath.charAt(newPath.length() - 1);
            if ((FileName.DOS_SEPERATOR != lastChar) && (NIX_SEPARATOR != lastChar))
            {
                newPath.append(FileName.DOS_SEPERATOR);
            }
            if (-1 == indexOfFirstSeparator)
            {
                indexOfFirstSeparator = newPath.length() - 1;
            }
            newPath = newPath.append(newName).delete(0, indexOfFirstSeparator);
            result = newPath.toString().replace(NIX_SEPARATOR, FileName.DOS_SEPERATOR);
        }
        return result;
    }

    /**
     * Updates NFS cache for specified node. <code>newPath</code> equal to <code>null</code> determines that node should be deleted from the cache
     * 
     * @param nodeRef - {@link NodeRef} value of the target node
     * @param newPath - {@link String} value or <code>null</code> to determine new cache value for specified node
     */
    private void updateNfsCache(NodeRef nodeRef, String newPath)
    {
        int dbId = -1;
        if (cachedNodes.containsKey(nodeRef))
        {
            dbId = (null != cachedNodes.get(nodeRef)) ? (cachedNodes.get(nodeRef)) : (-1);
            cachedNodes.remove(nodeRef);
        }
        else
        {
            if (nodeService.exists(nodeRef))
            {
                dbId = DefaultTypeConverter.INSTANCE.intValue(nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
            }
        }

        ShareDetails shareDetails = findShareDetailsForId(dbId);
        if (null != shareDetails)
        {
            if (null != newPath)
            {
                shareDetails.getFileIdCache().addPath(dbId, newPath);

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Path='" + newPath + "' in cache was set for NodeRef='" + nodeRef + "', dbId ='" + dbId + "'");
                }
            }
            else
            {
                shareDetails.getFileIdCache().deletePath(dbId);

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Cache field for node with NodeRef='" + nodeRef + "', dbId='" + dbId + "' was removed");
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NfsServerNodeMonitor)
        {
            NfsServerNodeMonitor converted = (NfsServerNodeMonitor) obj;
            return areEqual(targetDeviceName, converted.getTargetDeviceName()) && areEqual(targetStoreRef, converted.getTargetStoreRef());
        }
        return false;
    }

    private boolean areEqual(Object left, Object right)
    {
        return (null != left) ? (left.equals(right)) : (null == right);
    }

    @Override
    public int hashCode()
    {
        int result = (null != targetDeviceName) ? (targetDeviceName.hashCode()) : (31);
        return result * 37 + ((null != targetStoreRef) ? (targetStoreRef.hashCode()) : (43));
    }
}

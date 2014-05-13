/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.fileplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * File plan service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanServiceImpl extends ServiceBaseImpl
                                 implements FilePlanService
{
	/** I18N */
    private final static String MSG_DUP_ROOT = "rm.service.dup-root";
    private final static String MSG_ROOT_TYPE = "rm.service.root-type";
    private final static String MSG_PATH_NODE = "rm.service.path-node";
    private final static String MSG_INVALID_RM_NODE = "rm.service.invalid-rm-node";
    private final static String MSG_NO_ROOT = "rm.service.no-root";
    private final static String MSG_CONTAINER_PARENT_TYPE= "rm.service.container-parent-type";
    private final static String MSG_CONTAINER_TYPE = "rm.service.container-type";
    private final static String MSG_CONTAINER_EXPECTED = "rm.service.container-expected";

    /** File plan containers */
    private static final String NAME_UNFILED_CONTAINER = "Unfiled Records";
    private static final String NAME_HOLD_CONTAINER = "Holds";
    private static final String NAME_TRANSFER_CONTAINER = "Transfers";

    /** RM site file plan container */
    private static final String FILE_PLAN_CONTAINER = "documentLibrary";

    /** node DAO */
    private NodeDAO nodeDAO;
    
    /** file plan permission service */
    private FilePlanPermissionService filePlanPermissionService;
    
    /**
     * @param nodeDAO   node DAO
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    /**
     * @return  site service
     */
    protected SiteService getSiteService()
    {
        return (SiteService)applicationContext.getBean("siteService");
    }
    
    /**
     * @param filePlanPermissionService file plan permission service
     */
    public void setFilePlanPermissionService(FilePlanPermissionService filePlanPermissionService)
    {
        this.filePlanPermissionService = filePlanPermissionService;
    }   

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlans()
     */
    @Override
    public Set<NodeRef> getFilePlans()
    {
        return getFilePlans(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlans(org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
    public Set<NodeRef> getFilePlans(final StoreRef storeRef)
    {
        ParameterCheck.mandatory("storeRef", storeRef);

        final Set<NodeRef> results = new HashSet<NodeRef>();
        Set<QName> aspects = new HashSet<QName>(1);
        aspects.add(ASPECT_RECORDS_MANAGEMENT_ROOT);
        nodeDAO.getNodesWithAspects(aspects, Long.MIN_VALUE, Long.MAX_VALUE, new NodeDAO.NodeRefQueryCallback()
        {
            @Override
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                NodeRef nodeRef = nodePair.getSecond();
                if (storeRef.equals(nodeRef.getStoreRef()))
                {
                    results.add(nodeRef);
                }

                return true;
            }
        });
        return results;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getFilePlanBySiteId(java.lang.String)
     */
    @Override
    public NodeRef getFilePlanBySiteId(String siteId)
    {
        NodeRef filePlan = null;
        SiteService siteService = getSiteService();
        
        SiteInfo siteInfo = siteService.getSite(siteId);
        if (siteInfo != null)
        {
            if (siteService.hasContainer(siteId, FILE_PLAN_CONTAINER))
            {
                NodeRef nodeRef = siteService.getContainer(siteId, FILE_PLAN_CONTAINER);
                if (instanceOf(nodeRef, TYPE_FILE_PLAN))
                {
                    filePlan = nodeRef;
                }
            }
        }

        return filePlan;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#existsUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean existsUnfiledContainer(NodeRef filePlan)
    {
        return (getUnfiledContainer(filePlan) != null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef getUnfiledContainer(NodeRef filePlan)
    {
        return getFilePlanRootContainer(filePlan, NAME_UNFILED_CONTAINER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getHoldContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef getHoldContainer(NodeRef filePlan)
    {
        return getFilePlanRootContainer(filePlan, NAME_HOLD_CONTAINER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getTransferContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef getTransferContainer(NodeRef filePlan)
    {
        return getFilePlanRootContainer(filePlan, NAME_TRANSFER_CONTAINER);
    }

    /**
     *
     * @param filePlan
     * @param containerName
     * @return
     */
    private NodeRef getFilePlanRootContainer(NodeRef filePlan, String containerName)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        if (!isFilePlan(filePlan))
        {
            throw new AlfrescoRuntimeException("Unable to get the container " + containerName  + ", because passed node is not a file plan.");
        }

        NodeRef result = null;

        // try and get the unfiled record container
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(filePlan, ContentModel.ASSOC_CONTAINS, QName.createQName(RM_URI, containerName));
        if (assocs.size() > 1)
        {
            throw new AlfrescoRuntimeException("Unable to get unfiled conatiner " + containerName  + ".");
        }
        else if (assocs.size() == 1)
        {
            result = assocs.get(0).getChildRef();
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef createUnfiledContainer(NodeRef filePlan)
    {
        return createFilePlanRootContainer(filePlan, TYPE_UNFILED_RECORD_CONTAINER, NAME_UNFILED_CONTAINER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createHoldContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef createHoldContainer(NodeRef filePlan)
    {
        return createFilePlanRootContainer(filePlan, TYPE_HOLD_CONTAINER, NAME_HOLD_CONTAINER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createTransferContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef createTransferContainer(NodeRef filePlan)
    {
        return createFilePlanRootContainer(filePlan, TYPE_TRANSFER_CONTAINER, NAME_TRANSFER_CONTAINER);
    }

    /**
     *
     * @param filePlan
     * @param containerType
     * @param containerName
     * @param inheritPermissions
     * @return
     */
    private NodeRef createFilePlanRootContainer(NodeRef filePlan, QName containerType, String containerName)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        if (!isFilePlan(filePlan))
        {
            throw new AlfrescoRuntimeException("Unable to create file plan root container, because passed node is not a file plan.");
        }

        // create the properties map
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, containerName);

        // create the unfiled container
        NodeRef container = nodeService.createNode(
                        filePlan,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(RM_URI, containerName),
                        containerType,
                        properties).getChildRef();
        
        // setup the permissions
        filePlanPermissionService.setupPermissions(filePlan, container);

        return container;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("type", type);

        // Check the parent is not already an RM component node
        // ie: you can't create a rm root in an existing rm hierarchy
        if (isFilePlanComponent(parent))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_DUP_ROOT));
        }

        // Check that the passed type is a sub-type of rma:filePlan
        if (!TYPE_FILE_PLAN.equals(type) &&
            !dictionaryService.isSubClass(type, TYPE_FILE_PLAN))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_ROOT_TYPE, type.toString()));
        }

        // Build map of properties
        Map<QName, Serializable> rmRootProps = new HashMap<QName, Serializable>(1);
        if (properties != null && properties.size() != 0)
        {
            rmRootProps.putAll(properties);
        }
        rmRootProps.put(ContentModel.PROP_NAME, name);

        // Create the root
        ChildAssociationRef assocRef = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                type,
                rmRootProps);

        // TODO do we need to create role and security groups or is this done automatically?

        return assocRef.getChildRef();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return createFilePlan(parent, name, TYPE_FILE_PLAN, properties);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createFilePlan(NodeRef parent, String name)
    {
        return createFilePlan(parent, name, TYPE_FILE_PLAN, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName)
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type)
    {
        return createFilePlan(parent, name, type, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getNodeRefPath(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getNodeRefPath(NodeRef nodeRef)
    {
        LinkedList<NodeRef> nodeRefPath = new LinkedList<NodeRef>();
        try
        {
            getNodeRefPathRecursive(nodeRef, nodeRefPath);
        }
        catch (RuntimeException e)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PATH_NODE, nodeRef), e);
        }
        return nodeRefPath;
    }

    /**
     * Helper method to build a <b>NodeRef</b> path from the node to the RM root
     */
    private void getNodeRefPathRecursive(NodeRef nodeRef, LinkedList<NodeRef> nodeRefPath)
    {
        if (!isFilePlanComponent(nodeRef))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_INVALID_RM_NODE, ASPECT_FILE_PLAN_COMPONENT.toString()));
        }
        // Prepend it to the path
        nodeRefPath.addFirst(nodeRef);
        // Are we not at the root
        if (!isFilePlan(nodeRef))
        {
            ChildAssociationRef assocRef = nodeService.getPrimaryParent(nodeRef);
            if (assocRef == null)
            {
                // We hit the top of the store
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NO_ROOT));
            }
            // Recurse
            nodeRef = assocRef.getParentRef();
            getNodeRefPathRecursive(nodeRef, nodeRefPath);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("type", type);

        // Check that the parent is a container
        QName parentType = nodeService.getType(parent);
        if (!TYPE_RECORDS_MANAGEMENT_CONTAINER.equals(parentType) &&
            !dictionaryService.isSubClass(parentType, TYPE_RECORDS_MANAGEMENT_CONTAINER))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_PARENT_TYPE, parentType.toString()));
        }

        // Check that the the provided type is a sub-type of rm:recordCategory
        if (!TYPE_RECORD_CATEGORY.equals(type) &&
            !dictionaryService.isSubClass(type, TYPE_RECORD_CATEGORY))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_TYPE, type.toString()));
        }

        // Set the properties for the record category
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        if (properties != null && properties.size() != 0)
        {
            props.putAll(properties);
        }
        props.put(ContentModel.PROP_NAME, name);

        return nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (name.length() > QName.MAX_LENGTH ? name.substring(0, QName.MAX_LENGTH) : name)),
                type,
                props).getChildRef();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name)
    {
        return createRecordCategory(parent, name, TYPE_RECORD_CATEGORY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return createRecordCategory(parent, name, TYPE_RECORD_CATEGORY, properties);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type)
    {
        return createRecordCategory(parent, name, type, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getAllContained(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container)
    {
        return getAllContained(container, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getAllContained(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container, boolean deep)
    {
        return getContained(container, null, deep);
    }

    /**
     * Get contained nodes of a particular type.  If null return all.
     *
     * @param container container node reference
     * @param typeFilter type filter, null if none
     * @return {@link List}<{@link NodeRef> list of contained node references
     */
    private List<NodeRef> getContained(NodeRef container, QName typeFilter, boolean deep)
    {
        // Parameter check
        ParameterCheck.mandatory("container", container);

        // Check we have a container in our hands
        if (!isRecordCategory(container))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_EXPECTED));
        }

        List<NodeRef> result = new ArrayList<NodeRef>(1);
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(container, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef child = assoc.getChildRef();
            QName childType = nodeService.getType(child);
            if (typeFilter == null ||
                typeFilter.equals(childType) ||
                dictionaryService.isSubClass(childType, typeFilter))
            {
                result.add(child);
            }

            // Inspect the containers and add children if deep
            if (deep &&
                (TYPE_RECORD_CATEGORY.equals(childType) ||
                 dictionaryService.isSubClass(childType, TYPE_RECORD_CATEGORY)))
            {
                result.addAll(getContained(child, typeFilter, deep));
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getContainedRecordCategories(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container)
    {
        return getContainedRecordCategories(container, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getContainedRecordCategories(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container, boolean deep)
    {
        return getContained(container, TYPE_RECORD_CATEGORY, deep);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getContainedRecordFolders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container)
    {
        return getContainedRecordFolders(container, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService#getContainedRecordFolders(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep)
    {
        return getContained(container, TYPE_RECORD_FOLDER, deep);
    }
}

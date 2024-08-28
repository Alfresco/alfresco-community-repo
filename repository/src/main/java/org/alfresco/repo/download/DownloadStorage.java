/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.download;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.repo.download.cannedquery.DownloadEntity;
import org.alfresco.repo.download.cannedquery.GetDownloadsCannedQuery;
import org.alfresco.repo.download.cannedquery.GetDownloadsCannedQueryFactory;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible for the persistence of objects using lower-level
 * repo services such as the {@link NodeService}. The higher-level business logic around these CRUD calls
 * is contained within the {@link DownloadServiceImpl}.
 * 
 * @author Alex Miller
 */
public class DownloadStorage
{
    private static final Log log = LogFactory.getLog(DownloadStorage.class);
    
    // service dependencies
    private ImporterBootstrap bootstrap;
    private Repository        repositoryHelper;
    private NodeService       nodeService;
    private NodeService       noPermissionCheckNodeService;
    private NamespaceService  namespaceService;
    private NamedObjectRegistry<CannedQueryFactory<? extends Object>> queryRegistry;
    
    public void setImporterBootstrap(ImporterBootstrap bootstrap)
    {
        this.bootstrap = bootstrap;
    }
    
    public void setQueryRegistry(NamedObjectRegistry<CannedQueryFactory<? extends Object>> queryRegistry) 
    {
        this.queryRegistry = queryRegistry;
    }
    
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setNoPermissionCheckNodeService(NodeService noPermissionCheckNodeService)
    {
        this.noPermissionCheckNodeService = noPermissionCheckNodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * This method finds the SyncSet Definition Container NodeRef, creating one if it does not exist.
     * 
     * @return the syncset definition container
     */
    public NodeRef getOrCreateDowloadContainer()
    {
        NodeRef downloadsContainer = getContainer();
        
        if (downloadsContainer == null)
        {
            if (log.isInfoEnabled())
                log.info("Lazy creating the Downloads System Container ");

            downloadsContainer = SystemNodeUtils.getOrCreateSystemChildContainer(getContainerQName(), nodeService, repositoryHelper).getFirst();
        }
        return downloadsContainer;
    }
    
    private NodeRef getContainer() 
    {
        return SystemNodeUtils.getSystemChildContainer(getContainerQName(), nodeService, repositoryHelper);
    }

    private QName getContainerQName()
    {
        String name = bootstrap.getConfiguration().getProperty("system.downloads_container.childname");
        QName container = QName.createQName(name, namespaceService);
        return container;
    }
    
    
    public NodeRef createDownloadNode(boolean recursive)
    {
        NodeRef downloadsContainer = getOrCreateDowloadContainer();

        Map<QName, Serializable> downloadProperties = new HashMap<QName, Serializable>();
        downloadProperties.put(DownloadModel.PROP_RECURSIVE, recursive);
        
        ChildAssociationRef newChildAssoc = noPermissionCheckNodeService.createNode(downloadsContainer,
                                                                   ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN,
                                                                   DownloadModel.TYPE_DOWNLOAD,
                                                                   downloadProperties);
        
        final NodeRef downloadNodeRef = newChildAssoc.getChildRef();
        
        // MNT-11911 fix, add ASPECT_INDEX_CONTROL and property that not create indexes for search and not visible files/folders at 'My Documents' dashlet 
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(2);
        aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(downloadNodeRef, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);
         
        if (log.isDebugEnabled())
        {
            String downloadNodeRefString = "Download-NodeRef=" + downloadNodeRef;
            StringBuilder msg = new StringBuilder();
            msg.append("Created Download. ")
                    .append(downloadNodeRefString);
            log.debug(msg.toString());
        }
        return downloadNodeRef;
    }
    
    public void cancelDownload(NodeRef downloadNodeRef)
    {
        validateNode(downloadNodeRef);
        
        nodeService.setProperty(downloadNodeRef, DownloadModel.PROP_CANCELLED, true);
    }
    
    public boolean isCancelled(NodeRef downloadNodeRef)
    {
        validateNode(downloadNodeRef);
        
        return (Boolean)nodeService.getProperty(downloadNodeRef, DownloadModel.PROP_CANCELLED);
    }

    public void addNodeToDownload(NodeRef downloadNode, NodeRef nodeToAdd)
    {
       nodeService.createAssociation(downloadNode, nodeToAdd, DownloadModel.ASSOC_REQUESTED_NODES);
        
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Node added to Download-NodeRef '")
               .append(downloadNode).append("'. RequestedNode=")
               .append(nodeToAdd);
            log.debug(msg.toString());
        }
 
    }

    public DownloadRequest getDownloadRequest(NodeRef downloadNodeRef)
    {
        validateNode(downloadNodeRef);
        Map<QName, Serializable> properties = nodeService.getProperties(downloadNodeRef);
        
        List<AssociationRef> requestedNodes = nodeService.getTargetAssocs(downloadNodeRef, DownloadModel.ASSOC_REQUESTED_NODES);
        
        return new DownloadRequest((Boolean)properties.get(DownloadModel.PROP_RECURSIVE), requestedNodes, (String)properties.get(ContentModel.PROP_CREATOR));
    }

    private void validateNode(NodeRef downloadNodeRef)
    {
        if (!nodeService.getType(downloadNodeRef).equals(DownloadModel.TYPE_DOWNLOAD))
        {
            throw new IllegalArgumentException("Invalid node type for nodeRef: " + downloadNodeRef);
        }
    }

    public DownloadStatus getDownloadStatus(NodeRef downloadNodeRef)
    {
        validateNode(downloadNodeRef);
        Map<QName, Serializable> properties = nodeService.getProperties(downloadNodeRef);
        
        Long done = (Long)properties.get(DownloadModel.PROP_DONE);
        Long total = (Long)properties.get(DownloadModel.PROP_TOTAL);
        Long filesAdded = (Long)properties.get(DownloadModel.PROP_FILES_ADDED);
        Long totalFiles = (Long)properties.get(DownloadModel.PROP_TOTAL_FILES);

        if (log.isDebugEnabled())
        {
            log.debug("Status for Download-NodeRef: "+downloadNodeRef+": done: "+done+", total: "+total+", filesAdded: "+filesAdded+", totalFiles: "+totalFiles);
        }
        
        return new DownloadStatus(DownloadStatus.Status.valueOf((String)properties.get(DownloadModel.PROP_STATUS)),
                                  done != null ? done.longValue() : 0l,
                                  total != null ? total.longValue() : 0l,
                                  filesAdded != null ? filesAdded.longValue() : 0l,
                                  totalFiles != null ? totalFiles.longValue() : 0l);
    }

    public int getSequenceNumber(NodeRef nodeRef)
    {
        validateNode(nodeRef);
        Serializable sequenceNumber = nodeService.getProperty(nodeRef, DownloadModel.PROP_SEQUENCE_NUMBER);
        
        return ((Integer)sequenceNumber).intValue();
    }

    public void updateStatus(NodeRef nodeRef, DownloadStatus status)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Updating status for Download-NodeRef: "+nodeRef+" to status: "+status.getStatus());
        }
        validateNode(nodeRef);
        
        nodeService.setProperty(nodeRef, DownloadModel.PROP_STATUS, status.getStatus().toString());
        nodeService.setProperty(nodeRef, DownloadModel.PROP_DONE, Long.valueOf(status.getDone()));
        nodeService.setProperty(nodeRef, DownloadModel.PROP_TOTAL, Long.valueOf(status.getTotal()));
        nodeService.setProperty(nodeRef, DownloadModel.PROP_FILES_ADDED, status.getFilesAdded());
        nodeService.setProperty(nodeRef, DownloadModel.PROP_TOTAL_FILES, status.getTotalFiles());
    }

    /**
     * Get all the downloads created before before.
     */
    public List<List<DownloadEntity>> getDownloadsCreatedBefore(Date before, int batchSize, boolean cleanAllSysDownloadFolders)
    {
        List<NodeRef> allFoldersToBeCleaned = getAllFoldersToBeCleaned(cleanAllSysDownloadFolders);

        if (allFoldersToBeCleaned.isEmpty())
        {
            return Collections.emptyList();
        }
        List<List<DownloadEntity>> childDownloads = new ArrayList<>();

        for (NodeRef folderToBeCleaned : allFoldersToBeCleaned)
        {
            gatherDownloadFilesToBeCleanedFromFolder(childDownloads, before, batchSize, folderToBeCleaned);
        }
        return childDownloads;
    }

    private void gatherDownloadFilesToBeCleanedFromFolder(final List<List<DownloadEntity>> childDownloads, final Date before, final int batchSize,
        final NodeRef folderToBeCleaned)
    {
        // Grab the factory
        GetDownloadsCannedQueryFactory getDownloadCannedQueryFactory = (GetDownloadsCannedQueryFactory) queryRegistry
            .getNamedObject("downloadGetDownloadsCannedQueryFactory");

        // Run the canned query
        GetDownloadsCannedQuery cq = (GetDownloadsCannedQuery) getDownloadCannedQueryFactory.getDownloadsCannedQuery(folderToBeCleaned, before);

        // Execute the canned query
        CannedQueryResults<DownloadEntity> results = cq.execute();

        List<List<DownloadEntity>> downloadsInThisFolder = results.getPages();
        if (batchSize > 0)
        {
            int i = 0;
            while (childDownloads.size() < batchSize && downloadsInThisFolder.size() > i)
            {
                childDownloads.add(downloadsInThisFolder.get(i++));
            }
        }
        else
        {
            childDownloads.addAll(downloadsInThisFolder);
        }
    }

    private List<NodeRef> getAllFoldersToBeCleaned(boolean cleanAllSysDownloadFolders)
    {
        List<NodeRef> allFoldersToBeCleaned = new ArrayList<NodeRef>();

        if (cleanAllSysDownloadFolders)
        {
            for (NodeRef nodeRef : SystemNodeUtils.getSystemChildContainers(getContainerQName(), nodeService, repositoryHelper))
            {
                if (nodeRef != null)
                {
                    allFoldersToBeCleaned.add(nodeRef);
                }
            }
        }
        else
        {
            NodeRef container = getContainer();
            if (container != null)
            {
                allFoldersToBeCleaned.add(container);
            }
        }
        return allFoldersToBeCleaned;
    }

    /**
     * Delete the download node identified by nodeRef
     * @param nodeRef NodeRef
     */
    public void delete(NodeRef nodeRef)
    {
        validateNode(nodeRef);
        
        nodeService.deleteNode(nodeRef);
    }
}

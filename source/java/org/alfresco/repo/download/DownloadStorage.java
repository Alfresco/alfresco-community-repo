/*
 * Copyright 2005-2012 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.download;

import java.io.Serializable;
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
 * This class is responsible for the persistence of {@link DownloadDefinition} objects using lower-level
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
        
        ChildAssociationRef newChildAssoc = nodeService.createNode(downloadsContainer,
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
            StringBuilder msg = new StringBuilder();
            msg.append("Created Download. ")
               .append("', Download-NodeRef=");
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
        if (nodeService.getType(downloadNodeRef).equals(DownloadModel.TYPE_DOWNLOAD) == false)
        {
            throw new IllegalArgumentException("Invlaid node type for nodeRef:-" + downloadNodeRef);    
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
        validateNode(nodeRef);
        
        nodeService.setProperty(nodeRef, DownloadModel.PROP_STATUS, status.getStatus().toString());
        nodeService.setProperty(nodeRef, DownloadModel.PROP_DONE, new Long(status.getDone()));
        nodeService.setProperty(nodeRef, DownloadModel.PROP_TOTAL, new Long(status.getTotal()));
        nodeService.setProperty(nodeRef, DownloadModel.PROP_FILES_ADDED, status.getFilesAdded());
        nodeService.setProperty(nodeRef, DownloadModel.PROP_TOTAL_FILES, status.getTotalFiles());
    }

    /**
     * Get all the downloads created before before.
     */
    public List<List<DownloadEntity>> getDownloadsCreatedBefore(Date before)
    {
        NodeRef container = getContainer();
        
        if (container == null)
        {
            return Collections.emptyList();
        }

        // Grab the factory
        GetDownloadsCannedQueryFactory getDownloadCannedQueryFactory = 
                    (GetDownloadsCannedQueryFactory)queryRegistry.getNamedObject("downloadGetDownloadsCannedQueryFactory");
        
        // Run the canned query
        GetDownloadsCannedQuery cq = (GetDownloadsCannedQuery)getDownloadCannedQueryFactory.getDownloadsCannedQuery(container, before);
        
        // Execute the canned query
        CannedQueryResults<DownloadEntity> results = cq.execute();
        
        return results.getPages();
    }

    /**
     * Delete the download node identified by nodeRef
     * @param nodeRef
     */
    public void delete(NodeRef nodeRef)
    {
        validateNode(nodeRef);
        
        nodeService.deleteNode(nodeRef);
    }
}

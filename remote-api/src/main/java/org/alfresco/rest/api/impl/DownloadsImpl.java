/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ObjectStorageProps;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.rest.api.Downloads;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Download;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.ArchivedIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author cpopa
 *
 */
public class DownloadsImpl implements Downloads
{
    private static Logger logger = LoggerFactory.getLogger(Downloads.class);

    private DownloadService downloadService;
    private ModuleService moduleService;
    private NodeService nodeService;
    private ContentService contentService;
    private Nodes nodes;
    private PermissionService permissionService;
    private int archiveCheckLimit;
    public static final String DEFAULT_ARCHIVE_NAME = "archive.zip";
    public static final String DEFAULT_ARCHIVE_EXTENSION = ".zip";
    public static final String [] CLOUD_CONNECTOR_MODULES = {"org_alfresco_integrations_AzureConnector", "org_alfresco_integrations_S3Connector"};

    public void setDownloadService(DownloadService downloadService)
    {
        this.downloadService = downloadService;
    }

    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }    

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setArchiveCheckLimit(int checkLimit)
    {
        this.archiveCheckLimit = checkLimit;
    }

    @Override
    public Download createDownloadNode(Download download)
    {
        checkEmptyNodeIds(download);
        
        checkDuplicateNodeId(download);
        
        NodeRef[] zipContentNodeRefs = validateAndGetNodeRefs(download);
        
        checkNodeIdsReadPermission(zipContentNodeRefs);
        
        checkArchiveStatus(zipContentNodeRefs, archiveCheckLimit);

        NodeRef zipNodeRef = downloadService.createDownload(zipContentNodeRefs, true);
        
        String archiveName = zipContentNodeRefs.length > 1 ?
                                 DEFAULT_ARCHIVE_NAME : 
                                 nodeService.getProperty(zipContentNodeRefs[0], ContentModel.PROP_NAME) + DEFAULT_ARCHIVE_EXTENSION;
        
        nodeService.setProperty(zipNodeRef, ContentModel.PROP_NAME, archiveName);
        Download downloadInfo = getStatus(zipNodeRef);
        return downloadInfo;
    }

    @Override
    public Download getDownloadStatus(String downloadNodeId)
    {
        NodeRef downloadNodeRef = nodes.validateNode(downloadNodeId);
        
        checkIsDownloadNodeType(downloadNodeRef);
        
        Download downloadInfo = getStatus(downloadNodeRef);
        return downloadInfo;
    }

    @Override
    public void cancel(String downloadNodeId)
    {
        NodeRef downloadNodeRef = nodes.validateNode(downloadNodeId);
        checkIsDownloadNodeType(downloadNodeRef);
        
        downloadService.cancelDownload(downloadNodeRef);
    }    

    protected NodeRef[] validateAndGetNodeRefs(Download download)
    {
        return download.getNodeIds().stream()
                                    .map(nodeRef -> nodes.validateNode(nodeRef))
                                    .toArray(NodeRef[]::new);
    }

    protected void checkNodeIdsReadPermission(NodeRef[] zipContentNodeRefs)
    {
        for (NodeRef nodeRef : zipContentNodeRefs)
        {
            if (permissionService.hasReadPermission(nodeRef).equals(AccessStatus.DENIED)){
                throw new PermissionDeniedException();
            }
        }
    }

    protected void checkDuplicateNodeId(Download download)
    {
        if(download.getNodeIds().size() != new HashSet<String>(download.getNodeIds()).size()){
            throw new InvalidArgumentException("Cannot specify the same nodeId twice");
        }
    }

    protected void checkEmptyNodeIds(Download download)
    {
        if (download.getNodeIds().size() == 0)
        {
            throw new InvalidArgumentException("Cannot create an archive with 0 entries.");
        }
    }
    
    protected void checkIsDownloadNodeType(NodeRef downloadNodeRef)
    {
        QName nodeIdType = this.nodeService.getType(downloadNodeRef);
        
        if(!nodeIdType.equals(DownloadModel.TYPE_DOWNLOAD)){ 
            throw new InvalidArgumentException("Please specify the nodeId of a download node."); 
        }
    }    

    private Download getStatus(NodeRef downloadNodeRef)
    {
        DownloadStatus status = downloadService.getDownloadStatus(downloadNodeRef);
        Download downloadInfo = new Download();
        downloadInfo.setId(downloadNodeRef.getId());
        downloadInfo.setBytesAdded(status.getDone());
        downloadInfo.setFilesAdded(status.getFilesAdded());
        downloadInfo.setStatus(status.getStatus());
        downloadInfo.setTotalFiles(status.getTotalFiles());
        downloadInfo.setTotalBytes(status.getTotal());
        return downloadInfo;
    }

    /**
     * Checks the supplied nodes for any content that is archived.
     * Any folders will be expanded and their children checked.
     * A limit can be applied to prevent large sized requests preventing the asynchronous call to start.
     * 
     * @param nodeRefs
     * @param checkLimit The maximum number of nodes to check, set to -1 for no limit
     * @see #checkArchiveStatus(NodeRef[], int, Set)
     */
    @Experimental
    protected void checkArchiveStatus(NodeRef[] nodeRefs, int checkLimit) 
    {
        if (canCheckArchived()) 
        {
            checkArchiveStatus(nodeRefs, checkLimit, null);
        }
    }

    /**
     * Checks the supplied nodes for any content that is archived.
     * Any folders will be expanded and their children checked.
     * A limit can be applied to prevent large sized requests preventing the asynchronous call to start.
     * The cache is used to prevent duplication of checks, as it is possible to provide a folder and its contents as 
     * separate nodes in the download request.
     * 
     * @param nodeRefs
     * @param checkLimit The maximum number of nodes to check, set to -1 for no limit
     * @param cache Tracks nodes that we have already checked, if null an empty cache will be created
     */
    @Experimental
    private void checkArchiveStatus(NodeRef[] nodeRefs, int checkLimit, Set<NodeRef> cache)
    {
        // Create the cache for recursive calls.
        if (cache == null) 
        {
            cache = new HashSet<NodeRef>();
        }

        Set<NodeRef> folders = new HashSet<NodeRef>();
        for (NodeRef nodeRef : nodeRefs) 
        {
            // We hit the number of nodes we want to check.
            if (cache.size() == checkLimit) 
            {
                if (logger.isInfoEnabled())
                {
                    logger.info(
                        String.format(
                                "Maximum check of %d reached for archived content. No more checks will be performed and download will still be created.",
                                checkLimit));
                }
                return;
            }
            // Already checked this node, we can skip.
            if (cache.contains(nodeRef)) 
            {
                continue;
            }

            QName qName = nodeService.getType(nodeRef);
            if (qName.equals(ContentModel.TYPE_FOLDER))
            {
                // We'll check the child nodes at the end in case there are other nodes in this loop that is archived.
                folders.add(nodeRef); 
            }
            else if (qName.equals(ContentModel.TYPE_CONTENT))
            {
                Map<String, String> props = contentService.getStorageProperties(nodeRef, qName);
                if (!props.isEmpty() && Boolean.valueOf(props.get(ObjectStorageProps.X_ALF_ARCHIVED.getValue())))
                {
                    throw new ArchivedIOException("One or more nodes' content is archived and not accessible.");
                }
            }
            cache.add(nodeRef); // No need to check this node again.
        }

        // We re-run the folder contents at the end in case we hit content that is archived in the first loop and can stop early.
        for (NodeRef nodeRef : folders) 
        {
            NodeRef[] childRefs = nodeService.getChildAssocs(nodeRef).stream()
                                                                     .map(childAssoc -> childAssoc.getChildRef())
                                                                     .toArray(NodeRef[]::new);
            checkArchiveStatus(childRefs, checkLimit, cache); // We'll keep going until we have no more folders in children.
        }
    }

    @Experimental
    protected boolean canCheckArchived() 
    {
        return Arrays.stream(CLOUD_CONNECTOR_MODULES).anyMatch(m-> moduleService.getModule(m) != null);
    }
}

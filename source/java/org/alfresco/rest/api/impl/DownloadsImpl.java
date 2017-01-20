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

import java.util.HashSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.rest.api.Downloads;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Download;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author cpopa
 *
 */
public class DownloadsImpl implements Downloads
{
    private DownloadService downloadService;
    private NodeService nodeService;
    private Nodes nodes;
    private PermissionService permissionService;
    public static final String DEFAULT_ARCHIVE_NAME = "archive.zip";
    public static final String DEFAULT_ARCHIVE_EXTENSION = ".zip";

    public void setDownloadService(DownloadService downloadService)
    {
        this.downloadService = downloadService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }    

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    @Override
    public Download createDownloadNode(Download download)
    {
        checkEmptyNodeIds(download);
        
        checkDuplicateNodeId(download);
        
        NodeRef[] zipContentNodeRefs = validateAndGetNodeRefs(download);
        
        checkNodeIdsReadPermission(zipContentNodeRefs);
        
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
        downloadInfo.setDownloadId(downloadNodeRef.getId());
        downloadInfo.setDone(status.getDone());
        downloadInfo.setFilesAdded(status.getFilesAdded());
        downloadInfo.setStatus(status.getStatus());
        downloadInfo.setTotalFiles(status.getTotalFiles());
        downloadInfo.setTotal(status.getTotal());
        return downloadInfo;
    }

}

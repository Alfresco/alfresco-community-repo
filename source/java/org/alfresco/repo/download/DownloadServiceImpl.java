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
package org.alfresco.repo.download;

import java.util.Date;
import java.util.List;

import org.alfresco.repo.download.cannedquery.DownloadEntity;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Implementation of the download service.
 * 
 * Persists the download reqest and then uses a local action service to execute
 * the {@link CreateDownloadArchiveAction}.
 *
 * @author Alex Miller
 */
public class DownloadServiceImpl implements DownloadService {

    // Dependencies
    private ActionServiceHelper actionServiceHelper;
    private DownloadStorage downloadStorage;
    private RetryingTransactionHelper transactionHelper;
    
    // Dependency setters
    public void setActionServiceHelper(ActionServiceHelper actionServiceHelper)
    {
        this.actionServiceHelper = actionServiceHelper;
    }
    
    public void setTransactionHelper(RetryingTransactionHelper transactionHelper)
    {
        this.transactionHelper = transactionHelper; 
    }

    public void setDownloadStorage(DownloadStorage downloadStorage)
    {
        this.downloadStorage = downloadStorage;
    }
    
	@Override
	public NodeRef createDownload(final NodeRef[] requestedNodes, final boolean recursive) {
	    ParameterCheck.mandatory("nodeRefs", requestedNodes);
	    if (requestedNodes.length < 1)
	    {
	        throw new IllegalArgumentException("Need at least 1 node ref");
	    }
	    
	    // This is done in a new transaction to avoid node not found errors when the zip creation occurs 
	    // on a remote transformation server. 
        NodeRef downloadNode = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {

            @Override
            public NodeRef execute() throws Throwable
            {
                //Create a download node
                NodeRef downloadNode = downloadStorage.createDownloadNode(recursive);
                
                //Add requested nodes
                for (NodeRef node : requestedNodes)
                {
                   downloadStorage.addNodeToDownload(downloadNode, node);
                }
                
                return downloadNode;
            }
        }, false, true);
	    
	    //Trigger the action.
	    actionServiceHelper.executeAction(downloadNode);
	    
	    return downloadNode;
	}


	@Override
	public DownloadStatus getDownloadStatus(NodeRef downloadNode) {
		ParameterCheck.mandatory("downloadNode", downloadNode);
		
		return downloadStorage.getDownloadStatus(downloadNode);
    }
	
    /*
     * @see org.alfresco.service.cmr.download.DownloadService#deleteDownloads(java.util.Date)
     */
    @Override
    public void deleteDownloads(Date before)
    {
        List<List<DownloadEntity>> downloadPages = downloadStorage.getDownloadsCreatedBefore(before);
        for (List<DownloadEntity> page : downloadPages)
        {
            for (DownloadEntity download : page) 
            {
                downloadStorage.delete(download.getNodeRef());
            }
        }
        
    }

    /*
     * @see org.alfresco.service.cmr.download.DownloadService#cancelDownload(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void cancelDownload(NodeRef downloadNodeRef)
    {
        ParameterCheck.mandatory("downloadNodeRef", downloadNodeRef);
        
        downloadStorage.cancelDownload(downloadNodeRef);
    }
    
}

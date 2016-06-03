package org.alfresco.repo.download;

import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementation class responsible for update the status of a download node. 
 *
 * @author Alex Miller
 */
public class DownloadStatusUpdateServiceImpl implements DownloadStatusUpdateService
{

    // Dependencies
    private DownloadStorage storage;
    
    // Dependency setters
    public void setStorage(DownloadStorage storage)
    {
        this.storage = storage;
    }
    
    @Override
    public void update(NodeRef nodeRef, DownloadStatus status, int sequenceNumber)
    {
        
        // Update the status of the download node, if and only if sequenceNumber is
        // greater than the sequence number of the last update. 
        int currentSequenceNumber = storage.getSequenceNumber(nodeRef);
        
        if (currentSequenceNumber < sequenceNumber)
        {
            storage.updateStatus(nodeRef, status);
        }
    }

}

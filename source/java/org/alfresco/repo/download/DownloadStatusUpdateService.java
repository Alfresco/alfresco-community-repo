package org.alfresco.repo.download;

import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for updating the status of a download.
 * 
 * @author Alex Miller
 */
public interface DownloadStatusUpdateService
{
    /**
     * Update and persist the status of the download.
     * 
     * Implementations should only do this if sequenceNumber is greater than
     * the sequenceNumber of the previous update, to prevent out of order 
     * updates.
     * 
     * @param nodeRef The download node, whose status is to be updated.
     * @param status The new status
     * @param sequenceNumber int
     */
    void update(NodeRef nodeRef, DownloadStatus status, int sequenceNumber);
}

package org.alfresco.service.cmr.download;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for updating the status of a download.
 * 
 * @author Alex Miller
 */
public interface DownloadStatusUpdateService
{

    void update(NodeRef nodeRef, DownloadStatus status, int sequenceNumber);
}

package org.alfresco.service.cmr.download;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Zip download service.
 * 
 * Implementations are responsible for triggering the Zip creation process and
 * reporting on the status of the of this process.
 *
 * @author Alex Miller
 */
public interface DownloadService
{
    /**
     * Start the creation of a downlaodable archive file containing the content
     * from the given nodeRefs.
     * 
     * Implementations are expected to do this asynchronously, with clients 
     * using the returned NodeRef to check on progress.

     * Initially, only zip files will be supported, however this could be 
     * extended in the future, to support additional archive types.
     * 
     * @param nodeRefs NodeRefs of content to be added to the archive file
     * @param recusirsive Recurse into container nodes
     * @return Reference to node which will eventually contain the archive file
     */
    public NodeRef createDownload(NodeRef[] nodeRefs, boolean recusirsive);
    
    /**
     * Get the status of the of the download identified by downloadNode.
     */
    public DownloadStatus getDownloadStatus(NodeRef downloadNode);

    /**
     * Delete downloads created before before.
     * 
     * @param before Date
     */
    public void deleteDownloads(Date before);
    
    /**
     * Cancel a download request
     * 
     * @param downloadNodeRef NodeRef of the download to cancel
     */
    public void cancelDownload(NodeRef downloadNodeRef);
}

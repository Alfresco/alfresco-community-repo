package org.alfresco.repo.download;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ActionServiceHelper interface.
 * 
 * Allows the download service to switch between executing the zip creation process in the current alfresco node,
 * or on a remote node.
 *  
 * @author Alex Miller
 */
public interface ActionServiceHelper
{

    /**
     * Implementations should trigger the CreateDownloadArchiveAction on the provided downloadNode
     * 
     * @param downloadNode NodeRef
     */
    void executeAction(NodeRef downloadNode);

}

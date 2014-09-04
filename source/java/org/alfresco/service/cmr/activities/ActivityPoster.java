package org.alfresco.service.cmr.activities;

import org.alfresco.repo.Client;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A consolidated services for posting file folder activities.
 *
 * @author Gethin James
 */
public interface ActivityPoster
{

    public static final String DOWNLOADED = "org.alfresco.documentlibrary.file-downloaded";
    /**
     * Posts file folder activity.
     * @param activityType required
     * @param path optional
     * @param tenantDomain optional
     * @param siteId required
     * @param parentNodeRef optional
     * @param nodeRef required
     * @param fileName required
     * @param appTool required
     * @param client required
     * @param fileInfo optional
     */
    void postFileFolderActivity(String activityType, String path, String tenantDomain,
                String siteId, NodeRef parentNodeRef, NodeRef nodeRef, String fileName,
                String appTool, Client client, FileInfo fileInfo);

}

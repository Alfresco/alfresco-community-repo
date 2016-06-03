package org.alfresco.repo.webdav;

import org.alfresco.service.cmr.model.FileInfo;

/**
 * WebDAV methods can ActivityPoster to create entries in the activity feed.
 * 
 * @author Matt Ward
 */
public interface WebDAVActivityPoster
{
    /**
     * @param siteId
     * @param tenantDomain
     * @param path the path to the folder or <code>null</code> for files
     * @param nodeInfo
     * @throws WebDAVServerException
     */
    void postFileFolderAdded(
                String siteId,
                String tenantDomain,
                String path,
                FileInfo nodeInfo) throws WebDAVServerException;
    
    /**
     * @param siteId
     * @param tenantDomain
     * @param nodeInfo
     * @throws WebDAVServerException
     */
    void postFileFolderUpdated(
                String siteId,
                String tenantDomain,
                FileInfo nodeInfo) throws WebDAVServerException;
    
    /**
     * @param siteId
     * @param tenantDomain
     * @param parentPath
     * @param parentNodeInfo
     * @param contentNodeInfo
     * @throws WebDAVServerException
     */
    void postFileFolderDeleted(
                String siteId,
                String tenantDomain,
                String parentPath,
                FileInfo parentNodeInfo,
                FileInfo contentNodeInfo) throws WebDAVServerException;
}

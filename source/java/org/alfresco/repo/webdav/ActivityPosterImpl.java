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
package org.alfresco.repo.webdav;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDAV methods may use an instance of this class to post activity data.
 * 
 * @see WebDAVActivityPoster
 * @author Matt Ward
 */
public class ActivityPosterImpl implements WebDAVActivityPoster
{
    private String appTool;
    private ActivityPoster poster;
    
    protected static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol.activity");
    
    /**
     * Default constructor. 
     */
    public ActivityPosterImpl()
    {
    }

    /**
     * Constructor
     *
     * @param appTool
     * @param activityPoster
     */
    public ActivityPosterImpl(String appTool, ActivityPoster poster)
    {
        this.appTool = appTool;
        this.poster = poster;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderAdded(
                String siteId,
                String tenantDomain,
                String path,
                FileInfo nodeInfo) throws WebDAVServerException
    {
        postFileFolderActivity(nodeInfo.isFolder() ? ActivityType.FOLDER_ADDED : ActivityType.FILE_ADDED, siteId, tenantDomain, path, null, nodeInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderUpdated(
                String siteId,
                String tenantDomain,
                FileInfo nodeInfo) throws WebDAVServerException
    {
        if (! nodeInfo.isFolder())
        {
            postFileFolderActivity(ActivityType.FILE_UPDATED, siteId, tenantDomain, null, null, nodeInfo);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderDeleted(
                String siteId,
                String tenantDomain,
                String parentPath,
                FileInfo parentNodeInfo,
                FileInfo nodeInfo) throws WebDAVServerException
    {
        postFileFolderActivity(nodeInfo.isFolder() ? ActivityType.FOLDER_DELETED : ActivityType.FILE_DELETED, siteId, tenantDomain, parentPath, parentNodeInfo.getNodeRef(), nodeInfo);
    }
    
    
    private void postFileFolderActivity(
                String activityType,
                String siteId,
                String tenantDomain,
                String path,
                NodeRef parentNodeRef,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        String fileName = contentNodeInfo.getName();
        NodeRef nodeRef = contentNodeInfo.getNodeRef();
        
        try
        {
            poster.postFileFolderActivity(activityType, path, tenantDomain, siteId,
                                   parentNodeRef, nodeRef, fileName,
                                   appTool, Client.asType(ClientType.webdav),contentNodeInfo);
        }
        catch (AlfrescoRuntimeException are)
        {
            logger.error("Failed to post activity.", are);
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    

    public void setAppTool(String appTool)
    {
        this.appTool = appTool;
    }

    public void setPoster(ActivityPoster poster)
    {
        this.poster = poster;
    }
}

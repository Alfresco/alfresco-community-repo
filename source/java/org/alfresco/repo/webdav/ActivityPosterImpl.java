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

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.FileFilterMode.Client;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebDAV methods may use an instance of this class to post activity data.
 * 
 * @see ActivityPoster
 * @author Matt Ward
 */
// TODO consolidate with ActivityPost for OpenCMIS
public class ActivityPosterImpl implements ActivityPoster
{
    private String appTool;
    private ActivityService activityService;
    
    
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
     * @param activityService
     * @param nodeService
     * @param personService
     */
    public ActivityPosterImpl(String appTool, ActivityService activityService)
    {
        this.appTool = appTool;
        this.activityService = activityService;
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
        
        JSONObject json = createActivityJSON(tenantDomain, path, parentNodeRef, nodeRef, fileName);
        
        activityService.postActivity(
                    activityType,
                    siteId,
                    appTool,
                    json.toString(),
                    Client.webdav,
                    contentNodeInfo);
    }
    
    /**
     * Create JSON suitable for create, modify or delete activity posts. Returns a new JSONObject
     * containing appropriate key/value pairs.
     * 
     * @param tenantDomain
     * @param nodeRef
     * @param fileName
     * @throws WebDAVServerException
     * @return JSONObject
     */
    private JSONObject createActivityJSON(
                String tenantDomain,
                String path,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName) throws WebDAVServerException
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("nodeRef", nodeRef);
            
            if (parentNodeRef != null)
            {
                // Used for deleted files.
                json.put("parentNodeRef", parentNodeRef);
            }
            
            if (path != null)
            {
                // Used for deleted files and folders (added or deleted)
                json.put("page", "documentlibrary?path=" + path);
            }
            else
            {
                // Used for added or modified files.
                json.put("page", "document-details?nodeRef=" + nodeRef);
            }
            json.put("title", fileName);
            
            if (!tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                // Only used in multi-tenant setups.
                json.put("tenantDomain", tenantDomain);
            }
        }
        catch (JSONException error)
        {
            throw new WebDAVServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return json;
    }

    public void setAppTool(String appTool)
    {
        this.appTool = appTool;
    }

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
}

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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebDAV methods may use an instance of this class to post activity data.
 * 
 * @see ActivityPoster
 * @author Matt Ward
 */
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
    public void postFileAdded(
                String siteId,
                String tenantDomain,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(ActivityType.FILE_ADDED, siteId, tenantDomain, null, null, contentNodeInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileUpdated(
                String siteId,
                String tenantDomain,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(ActivityType.FILE_UPDATED, siteId, tenantDomain, null, null, contentNodeInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileDeleted(
                String siteId,
                String tenantDomain,
                String parentPath,
                FileInfo parentNodeInfo,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        postFileActivity(ActivityType.FILE_DELETED, siteId, tenantDomain, parentPath, parentNodeInfo.getNodeRef(), contentNodeInfo);
    }
    
    
    private void postFileActivity(
                String activityType,
                String siteId,
                String tenantDomain,
                String parentPath,
                NodeRef parentNodeRef,
                FileInfo contentNodeInfo) throws WebDAVServerException
    {
        String fileName = contentNodeInfo.getName();
        NodeRef nodeRef = contentNodeInfo.getNodeRef();
        
        JSONObject json = createActivityJSON(tenantDomain, parentPath, parentNodeRef, nodeRef, fileName);
        
        activityService.postActivity(
                    activityType,
                    siteId,
                    appTool,
                    json.toString());
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
                String parentPath,
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
            
            if (parentPath != null)
            {
                // Used for deleted files.
                json.put("page", "documentlibrary?path=" + parentPath);
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

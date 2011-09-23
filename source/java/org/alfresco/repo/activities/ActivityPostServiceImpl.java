/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.activities;

import java.sql.SQLException;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityPostService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Activity Post Service Implementation
 * 
 * @author janv
 */
public class ActivityPostServiceImpl implements ActivityPostService
{
    private static final Log logger = LogFactory.getLog(ActivityServiceImpl.class);
    
    private ActivityPostDAO postDAO;
    private TenantService tenantService;
    private int estGridSize = 1;
    
    private boolean userNamesAreCaseSensitive = false;
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setEstimatedGridSize(int estGridSize)
    {
        this.estGridSize = estGridSize;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void postActivity(String activityType, String siteId, String appTool, String activityData)
    {
        postActivity(activityType, siteId, appTool, activityData, ActivityPostEntity.STATUS.PENDING);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\""+PostLookup.JSON_NODEREF_LOOKUP+"\":\"").append(nodeRef.toString()).append("\"").append("}");
        
        postActivity(activityType, siteId, appTool, sb.toString(), ActivityPostEntity.STATUS.PENDING);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\""+PostLookup.JSON_NODEREF_LOOKUP+"\":\"").append(nodeRef.toString()).append("\"").append(",")
                      .append("\"name\":\"").append(name).append("\"")
                      .append("}");
        
        postActivity(activityType, siteId, appTool, sb.toString(), ActivityPostEntity.STATUS.PENDING);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name, QName typeQName, NodeRef parentNodeRef)
    {
        // primarily for delete node activities - eg. delete document, delete folder
        
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("typeQName", typeQName);
        ParameterCheck.mandatory("parentNodeRef", parentNodeRef);
          
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\""+PostLookup.JSON_NODEREF_LOOKUP+"\":\"").append(nodeRef.toString()).append("\"").append(",")
                      .append("\""+PostLookup.JSON_NAME+"\":\"").append(name).append("\"").append(",")
                      .append("\""+PostLookup.JSON_TYPEQNAME+"\":\"").append(typeQName.toPrefixString()).append("\"").append(",") // TODO toPrefixString does not return prefix ??!!
                      .append("\""+PostLookup.JSON_NODEREF_PARENT+"\":\"").append(parentNodeRef.toString()).append("\"")
                      .append("}");
        
        postActivity(activityType, siteId, appTool, sb.toString(), ActivityPostEntity.STATUS.PENDING);
    }
    
    private void postActivity(String activityType, String siteId, String appTool, String activityData, ActivityPostEntity.STATUS status)
    {
        String currentUser = getCurrentUser();
        
        try
        {
            // optional - default to empty string
            if (siteId == null)
            {
                siteId = "";
            }
            else if (siteId.length() > ActivityPostDAO.MAX_LEN_SITE_ID)
            {
                throw new IllegalArgumentException("Invalid siteId - exceeds " + ActivityPostDAO.MAX_LEN_SITE_ID + " chars: " + siteId);
            }
            
            // optional - default to empty string
            if (appTool == null)
            {
                appTool = "";
            }
            else if (appTool.length() > ActivityPostDAO.MAX_LEN_APP_TOOL_ID)
            {
                throw new IllegalArgumentException("Invalid app tool - exceeds " + ActivityPostDAO.MAX_LEN_APP_TOOL_ID + " chars: " + appTool);
            }
            
            // required
            ParameterCheck.mandatoryString("activityType", activityType);
            
            if (activityType.length() > ActivityPostDAO.MAX_LEN_ACTIVITY_TYPE)
            {
                throw new IllegalArgumentException("Invalid activity type - exceeds " + ActivityPostDAO.MAX_LEN_ACTIVITY_TYPE + " chars: " + activityType);
            }
            
            // optional - default to empty string
            if (activityData == null)
            {
                activityData = "";
            }
            
            if (AuthenticationUtil.isMtEnabled())
            {
                // MT share - required for canRead
                try
                {
                    JSONObject jo = new JSONObject(new JSONTokener(activityData));
                    
                    boolean update = false;
                    
                    if (! jo.isNull(PostLookup.JSON_NODEREF))
                    {
                        String nodeRefStr = jo.getString(PostLookup.JSON_NODEREF);
                        jo.put(PostLookup.JSON_NODEREF, tenantService.getName(new NodeRef(nodeRefStr)));
                        update = true;
                    }
                    
                    if (! jo.isNull(PostLookup.JSON_NODEREF_PARENT))
                    {
                        String nodeRefStr = jo.getString(PostLookup.JSON_NODEREF_PARENT);
                        jo.put(PostLookup.JSON_NODEREF_PARENT, tenantService.getName(new NodeRef(nodeRefStr)));
                        update = true;
                    }
                    
                    if (update)
                    {
                        activityData = jo.toString();
                    }
                }
                catch (JSONException e)
                {
                    throw new IllegalArgumentException("Invalid activity data - not valid JSON: " + e);
                }
            }
            
            if (activityData.length() > ActivityPostDAO.MAX_LEN_ACTIVITY_DATA)
            {
                throw new IllegalArgumentException("Invalid activity data - exceeds " + ActivityPostDAO.MAX_LEN_ACTIVITY_DATA + " chars: " + activityData);
            }
            
            // required
            ParameterCheck.mandatoryString("currentUser", currentUser);
            
            if (currentUser.length() > ActivityPostDAO.MAX_LEN_USER_ID)
            {
                throw new IllegalArgumentException("Invalid user - exceeds " + ActivityPostDAO.MAX_LEN_USER_ID + " chars: " + currentUser);
            }
        } 
        catch (IllegalArgumentException e)
        {
            // log error and throw exception
            logger.error(e);
            throw new IllegalArgumentException("Failed to post activity: " + e, e);
        }
        
        try
        {
            Date postDate = new Date();
            ActivityPostEntity activityPost = new ActivityPostEntity();
            activityPost.setUserId(currentUser);
            
            activityPost.setSiteNetwork(tenantService.getName(siteId));
            
            activityPost.setAppTool(appTool);
            activityPost.setActivityData(activityData);
            activityPost.setActivityType(activityType);
            activityPost.setPostDate(postDate);
            activityPost.setStatus(status.toString());
            activityPost.setLastModified(postDate);
            
            // hash the userid to generate a job task node
            int nodeCount = estGridSize;
            int userHashCode = currentUser.hashCode();
            int nodeHash = (userHashCode % nodeCount) + 1;
            
            activityPost.setJobTaskNode(nodeHash);
            
            try
            {
                long postId = postDAO.insertPost(activityPost);
                
                if (logger.isDebugEnabled()) 
                { 
                    activityPost.setId(postId);
                    logger.debug("Posted: " + activityPost); 
                }
            }
            catch (SQLException e) 
            {
                throw new AlfrescoRuntimeException("Failed to post activity: " + e, e);
            }
            catch (Throwable t) 
            {
                throw new AlfrescoRuntimeException("Failed to post activity: " + t, t);
            }
        } 
        catch (AlfrescoRuntimeException e)
        {
            // log error, subsume exception (for post activity)
            logger.error(e);
        }
    }
    
    private String getCurrentUser()
    {
        String userId = AuthenticationUtil.getRunAsUser();
        if ((userId != null) && (! userId.equals(AuthenticationUtil.SYSTEM_USER_NAME)) && (! userNamesAreCaseSensitive))
        {
            // user names are not case-sensitive
            userId = userId.toLowerCase();
        }
        
        return userId;
    }
}

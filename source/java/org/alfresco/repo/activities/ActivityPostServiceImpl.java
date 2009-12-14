/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.activities;

import java.sql.SQLException;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityPostService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Activity Post Service Implementation
 * 
 * @author janv
 */
public class ActivityPostServiceImpl implements ActivityPostService
{
    private static final Log logger = LogFactory.getLog(ActivityServiceImpl.class);
    
    private static final int MAX_LEN_USER_ID = 255;         // needs to match schema: feed_user_id, post_user_id
    private static final int MAX_LEN_SITE_ID = 255;         // needs to match schema: site_network
    private static final int MAX_LEN_ACTIVITY_TYPE = 255;   // needs to match schema: activity_type
    private static final int MAX_LEN_ACTIVITY_DATA = 4000;  // needs to match schema: activity_data
    private static final int MAX_LEN_APP_TOOL_ID = 36;      // needs to match schema: app_tool
    
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
        sb.append("{").append("\"nodeRef\":\"").append(nodeRef.toString()).append("\"").append("}");
        
        postActivity(activityType, siteId, appTool, sb.toString(), ActivityPostEntity.STATUS.PENDING);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\"nodeRef\":\"").append(nodeRef.toString()).append("\"").append(",")
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
        sb.append("{").append("\"nodeRef\":\"").append(nodeRef.toString()).append("\"").append(",")
                      .append("\"name\":\"").append(name).append("\"").append(",")
                      .append("\"typeQName\":\"").append(typeQName.toPrefixString()).append("\"").append(",") // TODO toPrefixString does not return prefix ??!!
                      .append("\"parentNodeRef\":\"").append(parentNodeRef.toString()).append("\"")
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
            else if (siteId.length() > MAX_LEN_SITE_ID)
            {
                throw new AlfrescoRuntimeException("Invalid siteId - exceeds " + MAX_LEN_SITE_ID + " chars: " + siteId);
            }
            
            // optional - default to empty string
            if (appTool == null)
            {
                appTool = "";
            }
            else if (appTool.length() > MAX_LEN_APP_TOOL_ID)
            {
                throw new AlfrescoRuntimeException("Invalid app tool - exceeds " + MAX_LEN_APP_TOOL_ID + " chars: " + appTool);
            }
            
            // required
            ParameterCheck.mandatoryString("activityType", activityType);
            
            if (activityType.length() > MAX_LEN_ACTIVITY_TYPE)
            {
                throw new AlfrescoRuntimeException("Invalid activity type - exceeds " + MAX_LEN_ACTIVITY_TYPE + " chars: " + activityType);
            }
            
            // optional - default to empty string
            if (activityData == null)
            {
                activityData = "";
            }
            else if (activityType.length() > MAX_LEN_ACTIVITY_DATA)
            {
                throw new AlfrescoRuntimeException("Invalid activity data - exceeds " + MAX_LEN_ACTIVITY_DATA + " chars: " + activityData);
            }
            
            // required
            ParameterCheck.mandatoryString("currentUser", currentUser);
            
            if (currentUser.length() > MAX_LEN_USER_ID)
            {
                throw new AlfrescoRuntimeException("Invalid user - exceeds " + MAX_LEN_USER_ID + " chars: " + currentUser);
            }
        } 
        catch (AlfrescoRuntimeException e)
        {
            // log error and throw exception
            logger.error(e);
            throw e;
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
        String userId = AuthenticationUtil.getFullyAuthenticatedUser();
        if ((userId != null) && (! userId.equals(AuthenticationUtil.SYSTEM_USER_NAME)) && (! userNamesAreCaseSensitive))
        {
            // user names are not case-sensitive
            userId = userId.toLowerCase();
        }
        
        return userId;
    }
}

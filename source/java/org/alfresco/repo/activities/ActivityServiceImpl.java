/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.activities.feed.ActivityFeedDAO;
import org.alfresco.repo.activities.feed.ActivityFeedDaoService;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.control.FeedControlDAO;
import org.alfresco.repo.activities.feed.control.FeedControlDaoService;
import org.alfresco.repo.activities.post.ActivityPostDAO;
import org.alfresco.repo.activities.post.ActivityPostDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.activities.FeedControl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

/**
 * Activity Service Implementation
 * 
 * @author janv
 */
public class ActivityServiceImpl implements ActivityService
{
    private static final Log logger = LogFactory.getLog(ActivityServiceImpl.class);
    
    private static final int MAX_LEN_USER_ID = 255;         // needs to match schema: feed_user_id, post_user_id
    private static final int MAX_LEN_SITE_ID = 255;         // needs to match schema: site_network
    private static final int MAX_LEN_ACTIVITY_TYPE = 255;   // needs to match schema: activity_type
    private static final int MAX_LEN_ACTIVITY_DATA = 4000;  // needs to match schema: activity_data
    private static final int MAX_LEN_APP_TOOL_ID = 36;      // needs to match schema: app_tool
    
    private ActivityPostDaoService postDaoService;
    private ActivityFeedDaoService feedDaoService;
    private FeedControlDaoService feedControlDaoService;
    private AuthorityService authorityService;
    private FeedGenerator feedGenerator;
    
    private int maxFeedItems = 100;
    
    private boolean userNamesAreCaseSensitive = false;

    public void setMaxFeedItems(int maxFeedItems)
    {
        this.maxFeedItems = maxFeedItems;
    }
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    public void setPostDaoService(ActivityPostDaoService postDaoService)
    {
        this.postDaoService = postDaoService;
    }
    
    public void setFeedDaoService(ActivityFeedDaoService feedDaoService)
    {
        this.feedDaoService = feedDaoService;
    }
    
    public void setFeedControlDaoService(FeedControlDaoService feedControlDaoService)
    {
        this.feedControlDaoService = feedControlDaoService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setFeedGenerator(FeedGenerator feedGenerator)
    {
        this.feedGenerator = feedGenerator;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void postActivity(String activityType, String network, String appTool, String activityData)
    {
        postActivity(activityType, network, appTool, activityData, ActivityPostDAO.STATUS.POSTED);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String network, String appTool, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\"nodeRef\":\"").append(nodeRef.toString()).append("\"").append("}");
        
        postActivity(activityType, network, appTool, sb.toString(), ActivityPostDAO.STATUS.PENDING);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void postActivity(String activityType, String network, String appTool, NodeRef nodeRef, String name)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        StringBuffer sb = new StringBuffer();
        sb.append("{").append("\"nodeRef\":\"").append(nodeRef.toString()).append("\"").append(",")
                      .append("\"name\":\"").append(name).append("\"")
                      .append("}");
        
        postActivity(activityType, network, appTool, sb.toString(), ActivityPostDAO.STATUS.PENDING);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String network, String appTool, NodeRef nodeRef, String name, QName typeQName, NodeRef parentNodeRef)
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
        
        postActivity(activityType, network, appTool, sb.toString(), ActivityPostDAO.STATUS.PENDING);
    }
    
    private void postActivity(String activityType, String siteNetwork, String appTool, String activityData, ActivityPostDAO.STATUS status)
    {
        String currentUser = AuthenticationUtil.getCurrentUserName();
        
        try
        {
            // optional - default to empty string
            if (siteNetwork == null)
            {
                siteNetwork = "";
            }
            else if (siteNetwork.length() > MAX_LEN_SITE_ID)
            {
                throw new AlfrescoRuntimeException("Invalid site network - exceeds " + MAX_LEN_SITE_ID + " chars: " + siteNetwork);
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
            else if ((! currentUser.equals(AuthenticationUtil.SYSTEM_USER_NAME)) && (! userNamesAreCaseSensitive))
            {
                // user names are not case-sensitive
                currentUser = currentUser.toLowerCase();
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
            ActivityPostDAO activityPost = new ActivityPostDAO();
            activityPost.setUserId(currentUser);
            activityPost.setSiteNetwork(siteNetwork);
            activityPost.setAppTool(appTool);
            activityPost.setActivityData(activityData);
            activityPost.setActivityType(activityType);
            activityPost.setPostDate(postDate);
            activityPost.setStatus(status.toString());
            activityPost.setLastModified(postDate);
            
            // hash the userid to generate a job task node
            int nodeCount = feedGenerator.getEstimatedGridSize();
            int userHashCode = currentUser.hashCode();
            int nodeHash = (userHashCode % nodeCount) + 1;
            
            activityPost.setJobTaskNode(nodeHash);
            
            try
            {
                long postId = postDaoService.insertPost(activityPost);
                
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
        } 
        catch (AlfrescoRuntimeException e)
        {
            // log error, subsume exception
            logger.error(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries(java.lang.String, java.lang.String, java.lang.String)
     */
    public List<String> getUserFeedEntries(String feedUserId, String format, String siteId)
    {
        // NOTE: siteId is optional
        ParameterCheck.mandatoryString("feedUserId", feedUserId);
        ParameterCheck.mandatoryString("format", format);
        
        List<String> activityFeedEntries = new ArrayList<String>();
        
        String currentUser = AuthenticationUtil.getCurrentUserName();
        if (currentUser != null)
        {
            if (! userNamesAreCaseSensitive)
            {
                feedUserId = feedUserId.toLowerCase();
                currentUser = currentUser.toLowerCase();
            }
            
            if ((! authorityService.isAdminAuthority(currentUser)) && (! currentUser.equals(feedUserId)))
            {
                throw new AlfrescoRuntimeException("Unable to get feed entries for '" + feedUserId + "' - currently logged in as '" + currentUser +"'");
            }
            
            try
            {
                List<ActivityFeedDAO> activityFeeds = null;
                if (siteId != null)
                {
                    activityFeeds = feedDaoService.selectUserFeedEntries(feedUserId, format, siteId);
                }
                else
                {
                    activityFeeds = feedDaoService.selectUserFeedEntries(feedUserId, format);
                }
                
                int count = 0;
                for (ActivityFeedDAO activityFeed : activityFeeds)
                {
                    count++;
                    if (count > maxFeedItems)
                    {
                        break;
                    }
                    activityFeedEntries.add(activityFeed.getJSONString());
                }
            }
            catch (SQLException se)
            {
                throw new AlfrescoRuntimeException("Unable to get user feed entries: " + se.getMessage());
            }
            catch (JSONException je)
            {    
                throw new AlfrescoRuntimeException("Unable to get user feed entries: " + je.getMessage());
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to get user feed entries - current user is null");
        }
        
        return activityFeedEntries;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getSiteFeedEntries(java.lang.String, java.lang.String)
     */
    public List<String> getSiteFeedEntries(String siteId, String format)
    {
        ParameterCheck.mandatoryString("siteId", siteId);
        ParameterCheck.mandatoryString("format", format);
        
        List<String> activityFeedEntries = new ArrayList<String>();

        String currentUser = AuthenticationUtil.getCurrentUserName();
        if (currentUser != null) 
        {
            // TODO - check whether site is public or private, if private, check whether user is member or admin - authorityService.isAdminAuthority(currentUser))
            try
            { 
                List<ActivityFeedDAO> activityFeeds = feedDaoService.selectSiteFeedEntries(siteId, format);
                
                int count = 0;
                for (ActivityFeedDAO activityFeed : activityFeeds)
                {
                    count++;
                    if (count > maxFeedItems)
                    {
                        break;
                    }
                    activityFeedEntries.add(activityFeed.getJSONString());
                }
            }
            catch (SQLException se)
            {
                throw new AlfrescoRuntimeException("Unable to get site feed entries: " + se.getMessage());
            }
            catch (JSONException je)
            {    
                throw new AlfrescoRuntimeException("Unable to get site feed entries: " + je.getMessage());
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to get site feed entries - current user is null");
        }
        
        return activityFeedEntries;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#setFeedControl(org.alfresco.service.cmr.activities.FeedControl)
     */
    public void setFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = AuthenticationUtil.getCurrentUserName();
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }

        try
        {
            if (! existsFeedControl(feedControl))
            {
                feedControlDaoService.insertFeedControl(new FeedControlDAO(userId, feedControl));
            }
        }
        catch (SQLException e) 
        {
          throw new AlfrescoRuntimeException("Failed to set feed control: " + e, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getFeedControls()
     */
    public List<FeedControl> getFeedControls()
    {
        String userId = AuthenticationUtil.getCurrentUserName();
        return getFeedControlsImpl(userId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getFeedControls(java.lang.String)
     */
    public List<FeedControl> getFeedControls(String userId)
    {
        ParameterCheck.mandatoryString("userId", userId);
        String currentUser = AuthenticationUtil.getCurrentUserName();
        
        if ((currentUser == null) || ((! currentUser.equals(AuthenticationUtil.getSystemUserName())) && (! currentUser.equals(userId)) && (! authorityService.isAdminAuthority(currentUser))))
        {
            throw new AlfrescoRuntimeException("Current user " + currentUser + " is not permitted to get feed controls for " + userId);
        }
        
        return getFeedControlsImpl(userId);
    }
        
    private List<FeedControl> getFeedControlsImpl(String userId)
    {
        ParameterCheck.mandatoryString("userId", userId);
        
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }
        
        try
        {
            List<FeedControlDAO> feedControlDaos = feedControlDaoService.selectFeedControls(userId);
            List<FeedControl> feedControls = new ArrayList<FeedControl>(feedControlDaos.size());
            for (FeedControlDAO feedControlDao : feedControlDaos)
            {
                feedControls.add(feedControlDao.getFeedControl());
            }
            
            return feedControls;
        }
        catch (SQLException e) 
        {
          throw new AlfrescoRuntimeException("Failed to get feed controls: " + e, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#deleteFeedControl(org.alfresco.service.cmr.activities.FeedControl)
     */
    public void unsetFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = AuthenticationUtil.getCurrentUserName();
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }
        
        try
        {
            feedControlDaoService.deleteFeedControl(new FeedControlDAO(userId, feedControl));
        }
        catch (SQLException e) 
        {
          throw new AlfrescoRuntimeException("Failed to unset feed control: " + e, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#existsFeedControl(java.lang.String, org.alfresco.service.cmr.activities.FeedControl)
     */
    public boolean existsFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = AuthenticationUtil.getCurrentUserName();
        
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }
        
        try
        {
            long id = feedControlDaoService.selectFeedControl(new FeedControlDAO(userId, feedControl));
            return (id != -1);
        }
        catch (SQLException e) 
        {
            throw new AlfrescoRuntimeException("Failed to query feed control: " + e, e);
        }
    }
}

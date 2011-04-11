/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.activities.feed.cleanup.FeedCleaner;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.FeedControlDAO;
import org.alfresco.repo.domain.activities.FeedControlEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityPostService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.activities.FeedControl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Activity Service Implementation
 * 
 * @author janv
 */
public class ActivityServiceImpl implements ActivityService, InitializingBean
{
    private static final Log logger = LogFactory.getLog(ActivityServiceImpl.class);
    
    private ActivityFeedDAO feedDAO;
    private FeedControlDAO feedControlDAO;
    private FeedCleaner feedCleaner;
    private AuthorityService authorityService;
    private TenantService tenantService;
    private SiteService siteService;
    private ActivityPostService activityPostService;
    
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
    
    public void setFeedDAO(ActivityFeedDAO feedDAO)
    {
        this.feedDAO = feedDAO;
    }
    
    public void setFeedControlDAO(FeedControlDAO feedControlDAO)
    {
        this.feedControlDAO = feedControlDAO;
    }
    
    public void setFeedCleaner(FeedCleaner feedCleaner)
    {
        this.feedCleaner = feedCleaner;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setActivityPostService(ActivityPostService activityPostService)
    {
        this.activityPostService = activityPostService;
    }
    
    
    /*(non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        int feedCleanerMaxFeedItems = feedCleaner.getMaxFeedSize();
        if (maxFeedItems > feedCleanerMaxFeedItems)
        {
            logger.warn("Cannot retrieve more items than feed cleaner max items (overriding "+maxFeedItems+" to "+feedCleanerMaxFeedItems+")");
            maxFeedItems = feedCleanerMaxFeedItems;
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void postActivity(String activityType, String siteId, String appTool, String activityData)
    {
        // delegate
        activityPostService.postActivity(activityType, siteId, appTool, activityData);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef)
    {
        // delegate
        activityPostService.postActivity(activityType, siteId, appTool, nodeRef);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name)
    {
        // delegate
        activityPostService.postActivity(activityType, siteId, appTool, nodeRef, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void postActivity(String activityType, String siteId, String appTool, NodeRef nodeRef, String name, QName typeQName, NodeRef parentNodeRef)
    {
        // delegate
        activityPostService.postActivity(activityType, siteId, appTool, nodeRef, name, typeQName, parentNodeRef);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries(java.lang.String, java.lang.String, java.lang.String)
     */
    public List<String> getUserFeedEntries(String feedUserId, String format, String siteId)
    {
        return getUserFeedEntries(feedUserId, format, siteId, false, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries(java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    public List<String> getUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers)
    {
        List<String> activityFeedEntries = new ArrayList<String>();
        
        try
        {
            List<ActivityFeedEntity> activityFeeds = getUserFeedEntries(feedUserId, format, siteId, excludeThisUser, excludeOtherUsers, -1);
            
            if (activityFeeds != null)
            {
                for (ActivityFeedEntity activityFeed : activityFeeds)
                {
                    activityFeedEntries.add(activityFeed.getJSONString());
                }
            }
        }
        catch (JSONException je)
        {    
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Unable to get user feed entries: " + je.getMessage());
            logger.error(are);
            throw are;
        }
        
        return activityFeedEntries;
    }
    
    public List<ActivityFeedEntity> getUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId)
    {
        // NOTE: siteId is optional
        ParameterCheck.mandatoryString("feedUserId", feedUserId);
        ParameterCheck.mandatoryString("format", format);
        
        if (! userNamesAreCaseSensitive)
        {
            feedUserId = feedUserId.toLowerCase();
        }
        
        String currentUser = getCurrentUser();
        if (! ((currentUser == null) || 
               (currentUser.equals(AuthenticationUtil.getSystemUserName())) ||
               (authorityService.isAdminAuthority(currentUser)) ||
               (currentUser.equals(feedUserId))))
        {
            throw new AccessDeniedException("Unable to get user feed entries for '" + feedUserId + "' - currently logged in as '" + currentUser +"'");
        }
        
        List<ActivityFeedEntity> result = new ArrayList<ActivityFeedEntity>();
        
        try
        {
            if (siteId != null)
            {
                siteId = tenantService.getName(siteId);
            }
            
            List<ActivityFeedEntity> activityFeeds = feedDAO.selectUserFeedEntries(feedUserId, format, siteId, excludeThisUser, excludeOtherUsers, minFeedId, maxFeedItems);
            
            for (ActivityFeedEntity activityFeed : activityFeeds)
            {
                activityFeed.setSiteNetwork(tenantService.getBaseName(activityFeed.getSiteNetwork()));
                result.add(activityFeed);
            }
        }
        catch (SQLException se)
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Unable to get user feed entries: " + se.getMessage());
            logger.error(are);
            throw are;
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getSiteFeedEntries(java.lang.String, java.lang.String)
     */
    public List<String> getSiteFeedEntries(String siteId, String format)
    {
        ParameterCheck.mandatoryString("siteId", siteId);
        ParameterCheck.mandatoryString("format", format);
        
        List<String> activityFeedEntries = new ArrayList<String>();
        
        try
        {
            if (siteService != null)
            {
                SiteInfo siteInfo = siteService.getSite(siteId);
                if (siteInfo == null)
                {
                    throw new AccessDeniedException("No such site: " + siteId);
                }
            }
            
            siteId = tenantService.getName(siteId);
            
            List<ActivityFeedEntity> activityFeeds = feedDAO.selectSiteFeedEntries(siteId, format, maxFeedItems);
            
            for (ActivityFeedEntity activityFeed : activityFeeds)
            {
                activityFeed.setSiteNetwork(tenantService.getBaseName(activityFeed.getSiteNetwork()));
                activityFeedEntries.add(activityFeed.getJSONString());
            }
        }
        catch (SQLException se)
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Unable to get site feed entries: " + se.getMessage());
            logger.error(are);
            throw are;
        }
        catch (JSONException je)
        {    
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Unable to get site feed entries: " + je.getMessage());
            logger.error(are);
            throw are;
        }
        
        return activityFeedEntries;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getMaxFeedItems()
     */
    public int getMaxFeedItems()
    {
        return this.maxFeedItems;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#setFeedControl(org.alfresco.service.cmr.activities.FeedControl)
     */
    public void setFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = getCurrentUser();
        
        if (userId == null)
        {
            throw new AlfrescoRuntimeException("Current user " + userId + " is not permitted to set feed control");
        }
        
        try
        {
            if (! existsFeedControl(feedControl))
            {
                feedControlDAO.insertFeedControl(new FeedControlEntity(userId, feedControl));
            }
        }
        catch (SQLException e) 
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Failed to set feed control: " + e, e);
            logger.error(are);
            throw are;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getFeedControls()
     */
    public List<FeedControl> getFeedControls()
    {
        String userId = getCurrentUser();
        return getFeedControlsImpl(userId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getFeedControls(java.lang.String)
     */
    public List<FeedControl> getFeedControls(String userId)
    {
        ParameterCheck.mandatoryString("userId", userId);
        
        if (! userNamesAreCaseSensitive)
        {
            userId = userId.toLowerCase();
        }
        
        String currentUser = getCurrentUser();
        
        if ((currentUser == null) || ((! currentUser.equals(AuthenticationUtil.getSystemUserName())) && (! currentUser.equals(userId)) && (! authorityService.isAdminAuthority(currentUser))))
        {
            throw new AlfrescoRuntimeException("Current user " + currentUser + " is not permitted to get feed controls for " + userId);
        }
        
        return getFeedControlsImpl(userId);
    }
        
    private List<FeedControl> getFeedControlsImpl(String userId)
    {
        ParameterCheck.mandatoryString("userId", userId);
        
        try
        {
            List<FeedControlEntity> feedControlDaos = feedControlDAO.selectFeedControls(userId);
            List<FeedControl> feedControls = new ArrayList<FeedControl>(feedControlDaos.size());
            for (FeedControlEntity feedControlDao : feedControlDaos)
            {
                feedControls.add(feedControlDao.getFeedControl());
            }
            
            return feedControls;
        }
        catch (SQLException e) 
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Failed to get feed controls: " + e, e);
            logger.error(are);
            throw are;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#deleteFeedControl(org.alfresco.service.cmr.activities.FeedControl)
     */
    public void unsetFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = getCurrentUser();
        
        if (userId == null)
        {
            throw new AlfrescoRuntimeException("Current user " + userId + " is not permitted to unset feed control");
        }
        
        try
        {
            feedControlDAO.deleteFeedControl(new FeedControlEntity(userId, feedControl));
        }
        catch (SQLException e) 
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Failed to unset feed control: " + e, e);
            logger.error(are);
            throw are;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#existsFeedControl(java.lang.String, org.alfresco.service.cmr.activities.FeedControl)
     */
    public boolean existsFeedControl(FeedControl feedControl)
    {
        ParameterCheck.mandatory("feedControl", feedControl);
        
        String userId = getCurrentUser();
        
        try
        {
            long id = feedControlDAO.selectFeedControl(new FeedControlEntity(userId, feedControl));
            return (id != -1);
        }
        catch (SQLException e) 
        {
            AlfrescoRuntimeException are = new AlfrescoRuntimeException("Failed to query feed control: " + e, e);
            logger.error(are);
            throw are;
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
    
    /*
    private FeedControl getTenantFeedControl(FeedControl feedControl)
    {
        // TODO
        return null;
    }
    
    private FeedControl getBaseFeedControl(FeedControl feedControl)
    {
        // TODO
        return null;
    }
    */
}

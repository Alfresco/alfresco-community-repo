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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
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
    private PersonService personService;
    private NodeService nodeService;
    
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
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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
     * @see org.alfresco.service.cmr.activities.ActivityService#postActivity(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */    
    public void postActivity(String activityType, String siteId, String appTool, String activityData, String userId)
    {
        // delegate
        activityPostService.postActivity(activityType, siteId, appTool, activityData, userId);
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
        return getUserFeedEntries(feedUserId, format, siteId, false, false, null, null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries(java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    public List<String> getUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers)
    {
        return getUserFeedEntries(feedUserId, format, siteId,excludeThisUser, excludeOtherUsers, null, null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.activities.ActivityService#getUserFeedEntries(java.lang.String, java.lang.String, java.lang.String, boolean, boolean, java.util.Set<String>, java.util.Set<String>)
     */
    public List<String> getUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, Set<String> userFilter, Set<String> actvityFilter)
    {
        List<String> activityFeedEntries = new ArrayList<String>();
        
        try
        {
            List<ActivityFeedEntity> activityFeeds = getUserFeedEntries(feedUserId, format, siteId, excludeThisUser, excludeOtherUsers, userFilter, actvityFilter, -1);
            
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
        return getUserFeedEntries(feedUserId, format, siteId, excludeThisUser, excludeOtherUsers, null, null, minFeedId);
    }
    
    public List<ActivityFeedEntity> getUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, Set<String> userFilter, Set<String> actvityFilter, long minFeedId)
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
               (authorityService.isAdminAuthority(currentUser)) ||
               (currentUser.equals(feedUserId)) ||
               (AuthenticationUtil.getSystemUserName().equals(this.tenantService.getBaseNameUser(currentUser)))))
        {
            throw new AccessDeniedException("Unable to get user feed entries for '" + feedUserId + "' - currently logged in as '" + currentUser +"'");
        }
        
        List<ActivityFeedEntity> result = new ArrayList<ActivityFeedEntity>();
        
        // avoid DB calls if filters are empty
        if(actvityFilter != null && actvityFilter.isEmpty())
        {
            return result;
        }
        if(userFilter != null && userFilter.isEmpty())
        {
            return result;
        }
        
        try
        {
            if (siteId != null)
            {
                siteId = tenantService.getName(siteId);
            }
            
            List<ActivityFeedEntity> activityFeeds = feedDAO.selectUserFeedEntries(feedUserId, format, siteId, excludeThisUser, excludeOtherUsers, minFeedId, maxFeedItems);

            // Create a local cache just for this method to map IDs of users to their avatar NodeRef. This
            // is local to the method because we only want to cache per request - there is not point in keeping
            // an instance cache because the data will become stale if a user changes their avatar.
            Map<String, NodeRef> userIdToAvatarNodeRefCache = new HashMap<String, NodeRef>();
            
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Selected feed entries for user : '" + feedUserId + "',\n using format : '" + format + "',\n for site : '" + siteId + "',\n excluding this user : '"
                        + excludeThisUser + "',\n excluding other users : '" + excludeOtherUsers + "',\n with min feed id : '" + minFeedId + "',\n with max feed items : '"
                        + maxFeedItems);
            }
            
            for (ActivityFeedEntity activityFeed : activityFeeds)
            {
                if (actvityFilter != null && !actvityFilter.contains(activityFeed.getActivityType())) {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Filtering " + activityFeed.toString() + " \n by the activity filter.");
                    }
                    continue;
                }
                
                if (userFilter != null && !userFilter.contains(activityFeed.getPostUserId())) {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Filtering " + activityFeed.toString() + " \n by the user filter.");
                    }
                    continue;
                }
                
                // In order to prevent unnecessary 304 revalidations on user avatars in the activity stream the 
                // activity posting user avatars will be retrieved and added to the activity feed. This will enable
                // avatars to be requested using the unique nodeRef which can be safely cached by the browser and
                // improve performance...
                NodeRef avatarNodeRef = null;
                String postUserId = activityFeed.getPostUserId();
                if (userIdToAvatarNodeRefCache.containsKey(postUserId))
                {
                    // If we've previously cached the users avatar, or if we've determine that the user doesn't
                    // have an avatar then use the cached data.
                    avatarNodeRef = userIdToAvatarNodeRefCache.get(postUserId);
                }
                else
                {
                    try
                    {
                        NodeRef postPerson = this.personService.getPerson(postUserId);
                        List<AssociationRef> assocRefs = this.nodeService.getTargetAssocs(postPerson, ContentModel.ASSOC_AVATAR);
                        if (!assocRefs.isEmpty())
                        {
                            // Get the avatar for the user id, set it in the activity feed and update the cache
                            avatarNodeRef = assocRefs.get(0).getTargetRef();
                        }
                    } 
                    catch (NoSuchPersonException e) 
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.warn("getUserFeedEntries: person no longer exists: "+postUserId);
                        }
                    }
                    
                    // Update the cache (setting null if there is no avatar for the user)...
                    userIdToAvatarNodeRefCache.put(postUserId, avatarNodeRef);
                }
                
                activityFeed.setPostUserAvatarNodeRef(avatarNodeRef);
                
                activityFeed.setSiteNetwork(tenantService.getBaseName(activityFeed.getSiteNetwork()));
                result.add(activityFeed);
                if (logger.isTraceEnabled())
                {
                    logger.trace("Selected user feed entry: " + activityFeed.toString());
                }
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
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Selected feed entries for site : '" + siteId + "',\n using format : '" + format);
            }
            
            for (ActivityFeedEntity activityFeed : activityFeeds)
            {
                activityFeed.setSiteNetwork(tenantService.getBaseName(activityFeed.getSiteNetwork()));
                activityFeedEntries.add(activityFeed.getJSONString());
                if (logger.isTraceEnabled())
                {
                    logger.trace("Selected site feed entry: " + activityFeed.toString());
                }
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
                if (logger.isDebugEnabled())
                {
                    logger.debug("Inserting feed control for siteId: " + feedControl.getSiteId() + ",\n appToolId : " + feedControl.getAppToolId() + ",\n for user : " + userId);
                }
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
        
        if ((currentUser == null) || 
            (! ((authorityService.isAdminAuthority(currentUser)) ||
                (currentUser.equals(userId)) ||
                (AuthenticationUtil.getSystemUserName().equals(this.tenantService.getBaseNameUser(currentUser))))))
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting feed controls for userId: " + userId);
            }
            List<FeedControl> feedControls = new ArrayList<FeedControl>(feedControlDaos.size());
            for (FeedControlEntity feedControlDao : feedControlDaos)
            {
                FeedControl feedCtrl = feedControlDao.getFeedControl();
                feedControls.add(feedCtrl);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Found feed control for userId: " + userId + ",\n appToolId : " + feedCtrl.getAppToolId() + ",\n siteId: " + feedCtrl.getSiteId());
                }
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleting feed control for siteId: " + feedControl.getSiteId() + ",\n appToolId : " + feedControl.getAppToolId() + ",\n for user : " + userId);
            }
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting feed control for siteId: " + feedControl.getSiteId() + ",\n appToolId : " + feedControl.getAppToolId() + ",\n for user : " + userId);
            }
            long id = feedControlDAO.selectFeedControl(new FeedControlEntity(userId, feedControl));
            boolean exists = (id != -1);
            if (logger.isDebugEnabled())
            {
                if(exists)
                {
                    logger.debug("The entry exists.");
                }
                else
                {
                    logger.debug("The entry doesn't exist.");
                }
            }
            return exists;
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
        if ((userId != null) &&
            (! userNamesAreCaseSensitive) &&
            (! AuthenticationUtil.getSystemUserName().equals(this.tenantService.getBaseNameUser(userId))))
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

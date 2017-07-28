/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.activities;

import java.util.List;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.service.NotAuditable;


/**
 * The activity service
 */
@AlfrescoPublicApi
public interface ActivityService extends ActivityPostService
{
    /*
     * Retrieve Feed Entries
     */
    
    /**
     * Retrieve user feed with optional site filter
     * 
     * Will return activities for all users across all sites, or optionally for all users for specified site.
     *
     * @param userId - required
     * @param siteId - optional, if set then will filter by given siteId else return all sites
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<String> getUserFeedEntries(String userId, String siteId);
    
    /**
     * Retrieve user feed with optional site filter and optional user filters
     * 
     * Will return activities for users across all sites, or optionally for users for specified site.
     * 
     * User filters are:
     * - all user activities   (excludeThisUser = false, excludeOtherUsers = false)
     * - other user activities (excludeThisUser = true,  excludeOtherUsers = false)
     * - my user activities    (excludeThisUser = false, excludeOtherUsers = true)
     * note: if both excludes are true then no activities will be returned.
     * 
     * @param userId     - required
     * @param siteId     - optional, if set then will filter by given siteId else return all sites
     * @param excludeThisUser    - if TRUE then will exclude activities for this user   (hence returning other users only)
     * @param excludeOtherUsers - if TRUE then will exclude activities for other users (hence returning this user only)
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<String> getUserFeedEntries(String userId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers);

    
    /**
     * Retrieve user feed with optional site filter and optional user filters
     * 
     * Will return activities for users across all sites, or optionally for users for specified site.
     * 
     * User filters are:
     * - all user activities   (excludeThisUser = false, excludeOtherUsers = false)
     * - other user activities (excludeThisUser = true,  excludeOtherUsers = false)
     * - my user activities    (excludeThisUser = false, excludeOtherUsers = true)
     * note: if both excludes are true then no activities will be returned.
     * 
     * @param userId     - required
     * @param siteId     - optional, if set then will filter by given siteId else return all sites
     * @param excludeThisUser    - if TRUE then will exclude activities for this user   (hence returning other users only)
     * @param excludeOtherUsers - if TRUE then will exclude activities for other users (hence returning this user only)
     * @param userFilter         - if not NULL then will only return activities of users in this set
     * @param actvityFilter      - if not NULL then will only return activities that are in this set
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<String> getUserFeedEntries(String userId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, Set<String> userFilter, Set<String> actvityFilter);
    
    /**
     * Retrieve user feed with optional site filter and optional user filters and optional min feed DB id
     * 
     * Will return activities for users across all sites, or optionally for users for specified site.
     * 
     * User filters are:
     * - all user activities   (excludeThisUser = false, excludeOtherUsers = false)
     * - other user activities (excludeThisUser = true,  excludeOtherUsers = false)
     * - my user activities    (excludeThisUser = false, excludeOtherUsers = true)
     * note: if both excludes are true then no activities will be returned.
     * 
     * @param feedUserId     - required
     * @param siteId     - optional, if set then will filter by given siteId else return all sites
     * @param excludeThisUser    - if TRUE then will exclude activities for this user   (hence returning other users only)
     * @param excludeOtherUsers - if TRUE then will exclude activities for other users (hence returning this user only)
     * @param minFeedId - inclusive from min feed DB id, if -1 then return all available
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<ActivityFeedEntity> getUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId);
    
    /**
     * Retrieve user feed with optional site filter and optional user filters and optional min feed DB id
     * 
     * Will return activities for users across all sites, or optionally for users for specified site.
     * 
     * User filters are:
     * - all user activities   (excludeThisUser = false, excludeOtherUsers = false)
     * - other user activities (excludeThisUser = true,  excludeOtherUsers = false)
     * - my user activities    (excludeThisUser = false, excludeOtherUsers = true)
     * note: if both excludes are true then no activities will be returned.
     * 
     * @param feedUserId     - required
     * @param siteId     - optional, if set then will filter by given siteId else return all sites
     * @param excludeThisUser    - if TRUE then will exclude activities for this user   (hence returning other users only)
     * @param excludeOtherUsers - if TRUE then will exclude activities for other users (hence returning this user only)
     * @param userFilter         - if not NULL then will only return activities of users in this set
     * @param actvityFilter      - if not NULL then will only return activities that are in this set
     * @param minFeedId - inclusive from min feed DB id, if -1 then return all available
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<ActivityFeedEntity> getUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, Set<String> userFilter, Set<String> actvityFilter, long minFeedId);
    
    @NotAuditable
    public PagingResults<ActivityFeedEntity> getPagedUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, PagingRequest pagingRequest);
    
    /**
     * Retrieve site feed
     *
     * @param siteId - required
     * @return list of JSON feed entries
     */
    @NotAuditable
    public List<String> getSiteFeedEntries(String siteId);
    
    
    /**
     * Return maximum configured item entries (per feed)
     * 
     * @return int
     */
    @NotAuditable
    public int getMaxFeedItems();
    
    /*
     * Manage User Feed Controls
     */
    
    /**
     * For current user, set feed control (opt-out) for a site or an appTool or a site/appTool combination
     *
     * @param feedControl - required
     */
    @NotAuditable
    public void setFeedControl(FeedControl feedControl);
    
    /**
     * For given user, get feed controls
     *
     * @param userId - required (must match
     * @return list of user feed controls
     */
    @NotAuditable
    public List<FeedControl> getFeedControls(String userId);
    
    /**
     * For current user, get feed controls
     *
     * @return list of user feed controls
     */
    @NotAuditable
    public List<FeedControl> getFeedControls();
    
    /**
     * For current user, unset feed control
     *
     * @param feedControl - required
     */
    @NotAuditable
    public void unsetFeedControl(FeedControl feedControl);
    
    /**
     * For current user, does the feed control exist ?
     *
     * @param feedControl - required
     * @return true, if user feed control exists
     */
    @NotAuditable
    public boolean existsFeedControl(FeedControl feedControl);
}

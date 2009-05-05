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
package org.alfresco.service.cmr.activities;

import java.util.List;

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
     * @param format - required
     * @param siteId - optional, if set then will filter by given siteId else return all sites
     * @return list of JSON feed entries
     */
    public List<String> getUserFeedEntries(String userId, String format, String siteId);
    
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
     * @param format     - required
     * @param siteId     - optional, if set then will filter by given siteId else return all sites
     * @param excludeThisUser    - if TRUE then will exclude activities for this user   (hence returning other users only)
     * @param excludeOthersUsers - if TRUE then will exclude activities for other users (hence returning this user only)
     * @return list of JSON feed entries
     */
    public List<String> getUserFeedEntries(String userId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers);
    
    /**
     * Retrieve site feed
     *
     * @param activityType - required
     * @param format - required
     * @return list of JSON feed entries
     */
    public List<String> getSiteFeedEntries(String siteId, String format);
    
    
    /*
     * Manage User Feed Controls
     */
    
    /**
     * For current user, set feed control (opt-out) for a site or an appTool or a site/appTool combination
     *
     * @param feedControl - required
     */
    public void setFeedControl(FeedControl feedControl);
    
    /**
     * For given user, get feed controls
     *
     * @param userId - required (must match
     * @return list of user feed controls
     */
    public List<FeedControl> getFeedControls(String userId);
    
    /**
     * For current user, get feed controls
     *
     * @return list of user feed controls
     */
    public List<FeedControl> getFeedControls();
    
    /**
     * For current user, unset feed control
     *
     * @param feedControl - required
     */
    public void unsetFeedControl(FeedControl feedControl);
    
    /**
     * For current user, does the feed control exist ?
     *
     * @param feedControl - required
     * @return true, if user feed control exists
     */
    public boolean existsFeedControl(FeedControl feedControl);
}

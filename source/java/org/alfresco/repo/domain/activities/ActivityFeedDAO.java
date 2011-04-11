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
package org.alfresco.repo.domain.activities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Interface for activity feed DAO service
 */
public interface ActivityFeedDAO extends ActivitiesDAO
{
    public static final int MAX_LEN_USER_ID = 255;            // needs to match schema: feed_user_id, post_user_id
    public static final int MAX_LEN_SITE_ID = 255;            // needs to match schema: site_network
    public static final int MAX_LEN_ACTIVITY_TYPE = 255;      // needs to match schema: activity_type
    public static final int MAX_LEN_ACTIVITY_SUMMARY = 4000;  // needs to match schema: activity_summary
    public static final int MAX_LEN_ACTIVITY_FORMAT = 255;    // needs to match schema: activity_format
    public static final int MAX_LEN_APP_TOOL_ID = 36;         // needs to match schema: app_tool
    
    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException;
    
    public int deleteFeedEntries(Date keepDate) throws SQLException;
    
    public int deleteUserFeedEntries(String feedUserId, String format, Date keepDate) throws SQLException;
    
    public int deleteUserFeedEntries(String feedUserId) throws SQLException;
    
    public int deleteSiteFeedEntries(String siteId, String format, Date keepDate) throws SQLException;
    
    public int deleteSiteFeedEntries(String siteUserId) throws SQLException;
    
    public List<ActivityFeedEntity> selectFeedsToClean(int maxFeedSize) throws SQLException;
    
    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedItems) throws SQLException;
    
    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteUserId, String format, int maxFeedItems) throws SQLException;
}

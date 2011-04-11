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
package org.alfresco.repo.domain.activities.ibatis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityFeedQueryEntity;

import com.ibatis.sqlmap.engine.execution.SqlExecutor;

public class ActivityFeedDAOImpl extends IBatisSqlMapper implements ActivityFeedDAO
{
    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException
    {
        Long id = (Long)getSqlMapClient().insert("alfresco.activities.insert_activity_feed", activityFeed);
        return (id != null ? id : -1);
    }
    
    public int deleteFeedEntries(Date keepDate) throws SQLException
    {
        return getSqlMapClient().delete("alfresco.activities.delete_activity_feed_entries_older_than_date", keepDate);
    }
    
    public int deleteSiteFeedEntries(String siteId) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        
        return getSqlMapClient().delete("alfresco.activities.delete_activity_feed_for_site_entries", params);
    }
    
    public int deleteSiteFeedEntries(String siteId, String format, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return getSqlMapClient().delete("alfresco.activities.delete_activity_feed_for_site_entries_older_than_date", params);
    }
    
    
    public int deleteUserFeedEntries(String feedUserId, String format, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return getSqlMapClient().delete("alfresco.activities.delete_activity_feed_for_feeduser_entries_older_than_date", params);
    }
    
    public int deleteUserFeedEntries(String feedUserId) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        
        return getSqlMapClient().delete("alfresco.activities.delete_activity_feed_for_feeduser_entries", params);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectFeedsToClean(int maxFeedSize) throws SQLException
    {
        return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_greater_than_max", maxFeedSize);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
        if (minFeedId > -1)
        {
            params.setMinId(minFeedId);
        }
        
        if (maxFeedSize < 0)
        {
            maxFeedSize = SqlExecutor.NO_MAXIMUM_RESULTS;
        }
        
        if (siteId != null)
        {
            // given site
            params.setSiteNetwork(siteId);
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedEntity>(0);
            }
            if ((!excludeThisUser) && (!excludeOtherUsers))
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser_and_site", params, 0, maxFeedSize);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser_others_and_site", params, 0, maxFeedSize);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser_me_and_site", params, 0, maxFeedSize);
            }
        }
        else
        {
            // all sites
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedEntity>(0);
            }
            if (!excludeThisUser && !excludeOtherUsers)
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser", params, 0, maxFeedSize);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser_others", params, 0, maxFeedSize);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_feeduser_me", params, 0, maxFeedSize);
            }
        }
        
        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }
       
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteId, String format, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        
        if (maxFeedSize < 0)
        {
            maxFeedSize = SqlExecutor.NO_MAXIMUM_RESULTS;
        }
        
        // for given site
        return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("alfresco.activities.select_activity_feed_for_site", params, 0, maxFeedSize);
    }
}

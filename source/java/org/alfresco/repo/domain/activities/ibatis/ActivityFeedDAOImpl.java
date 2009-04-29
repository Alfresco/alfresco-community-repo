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
package org.alfresco.repo.domain.activities.ibatis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;

public class ActivityFeedDAOImpl extends IBatisSqlMapper implements ActivityFeedDAO
{
    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException
    {
        Long id = (Long)getSqlMapClient().insert("insert.activity.feed", activityFeed);
        return (id != null ? id : -1);
    }
    
    public int deleteFeedEntries(Date keepDate) throws SQLException
    {
        return getSqlMapClient().delete("delete.activity.feed.entries.older.than.date", keepDate);
    }
    
    public int deleteSiteFeedEntries(String siteId, String format, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return getSqlMapClient().delete("delete.activity.feed.for.site.entries.older.than.date", params);
    }
    
    public int deleteUserFeedEntries(String feedUserId, String format, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return getSqlMapClient().delete("delete.activity.feed.for.feeduser.entries.older.than.date", params);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectFeedsToClean(int maxFeedSize) throws SQLException
    {
        return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.greater.than.max", maxFeedSize);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
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
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.and.site", params);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.others.and.site", params);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.me.and.site", params);
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
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser", params);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.others", params);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.me", params);
            }
        }
        
        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }
       
    @SuppressWarnings("unchecked")
    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteId, String format) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        
        // for given site
        return (List<ActivityFeedEntity>)getSqlMapClient().queryForList("select.activity.feed.for.site", params);
    }
}

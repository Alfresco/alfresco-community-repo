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
package org.alfresco.repo.activities.ibatis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.activities.feed.ActivityFeedDAO;
import org.alfresco.repo.activities.feed.ActivityFeedDaoService;

public class IBatisActivityFeedDaoServiceImpl extends IBatisSqlMapper implements ActivityFeedDaoService
{
    public long insertFeedEntry(ActivityFeedDAO activityFeed) throws SQLException
    {
        Long id = (Long)getSqlMapClient().insert("insert.activity.feed", activityFeed);
        return (id != null ? id : -1);
    }
    
    public int deleteFeedEntries(Date keepDate) throws SQLException
    {
        return getSqlMapClient().delete("delete.activity.feed.entries.older.than.date", keepDate);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedDAO> selectUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers) throws SQLException
    {
        ActivityFeedDAO params = new ActivityFeedDAO();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
        if (siteId != null)
        {
            // given site
            params.setSiteNetwork(siteId);
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedDAO>(0);
            }
            if ((!excludeThisUser) && (!excludeOtherUsers))
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.and.site", params);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.others.and.site", params);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.me.and.site", params);
            }
        }
        else
        {
            // all sites
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedDAO>(0);
            }
            if (!excludeThisUser && !excludeOtherUsers)
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser", params);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.others", params);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.me", params);
            }
        }
        
        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }
       
    @SuppressWarnings("unchecked")
    public List<ActivityFeedDAO> selectSiteFeedEntries(String siteId, String format) throws SQLException
    {
        ActivityFeedDAO params = new ActivityFeedDAO();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        
        // for given site
        return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.site", params);
    }
}

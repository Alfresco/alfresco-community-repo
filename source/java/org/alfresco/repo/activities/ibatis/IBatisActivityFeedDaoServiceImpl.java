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
package org.alfresco.repo.activities.ibatis;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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
    public List<ActivityFeedDAO> selectUserFeedEntries(String feedUserId, String format) throws SQLException
    {
        ActivityFeedDAO params = new ActivityFeedDAO();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
        // where feed user is me and post user is not me
        return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser", params);
    }
    
    @SuppressWarnings("unchecked")
    public List<ActivityFeedDAO> selectUserFeedEntries(String feedUserId, String format, String siteId) throws SQLException
    {
        ActivityFeedDAO params = new ActivityFeedDAO();
        params.setFeedUserId(feedUserId);
        params.setPostUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        params.setSiteNetwork(siteId);
        
        // where feed user is me and post user is not me
        return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.feeduser.and.site", params);
    }
       
    @SuppressWarnings("unchecked")
    public List<ActivityFeedDAO> selectSiteFeedEntries(String siteId, String format) throws SQLException
    {
        ActivityFeedDAO params = new ActivityFeedDAO();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        
        // where feed user is me and post user is not me
        return (List<ActivityFeedDAO>)getSqlMapClient().queryForList("select.activity.feed.for.site", params);
    }
}

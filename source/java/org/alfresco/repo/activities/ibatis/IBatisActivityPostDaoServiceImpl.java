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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.activities.post.ActivityPostDAO;
import org.alfresco.repo.activities.post.ActivityPostDaoService;

public class IBatisActivityPostDaoServiceImpl extends IBatisSqlMapper implements ActivityPostDaoService
{
    @SuppressWarnings("unchecked")
    public List<ActivityPostDAO> selectPosts(ActivityPostDAO activityPost) throws SQLException 
    {
        if ((activityPost.getJobTaskNode() != -1) &&
            (activityPost.getMinId() != -1) &&
            (activityPost.getMaxId() != -1) &&
            (activityPost.getStatus() != null))
        {
            return (List<ActivityPostDAO>)getSqlMapClient().queryForList("select.activity.posts", activityPost);
        }
        else if (activityPost.getStatus() != null)
        {
            return (List<ActivityPostDAO>)getSqlMapClient().queryForList("select.activity.posts.by.status.only", activityPost);
        }
        else
        {
            return new ArrayList<ActivityPostDAO>(0);
        }
    }

    public Long getMaxActivitySeq() throws SQLException 
    {
        return (Long)getSqlMapClient().queryForObject("select.activity.post.max.seq");
    }
    
    public Long getMinActivitySeq() throws SQLException 
    {
        return (Long)getSqlMapClient().queryForObject("select.activity.post.min.seq");
    }
    
    public Integer getMaxNodeHash() throws SQLException 
    {
        return (Integer)getSqlMapClient().queryForObject("select.activity.post.max.jobtasknode");
    }

    public int updatePost(long id, String siteNetwork, String activityData, ActivityPostDAO.STATUS status) throws SQLException
    {
        ActivityPostDAO post = new ActivityPostDAO();
        post.setId(id);
        post.setSiteNetwork(siteNetwork);
        post.setActivityData(activityData);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return getSqlMapClient().update("update.activity.post.data", post);
    }
    
    public int updatePostStatus(long id, ActivityPostDAO.STATUS status) throws SQLException
    {
        ActivityPostDAO post = new ActivityPostDAO();
        post.setId(id);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return getSqlMapClient().update("update.activity.post.status", post);
    }
    
    public int deletePosts(Date keepDate, ActivityPostDAO.STATUS status) throws SQLException
    {
        ActivityPostDAO params = new ActivityPostDAO();
        params.setPostDate(keepDate);
        params.setStatus(status.toString());
        
        return getSqlMapClient().delete("delete.activity.posts.older.than.date", params);
    }
    
    public long insertPost(ActivityPostDAO activityPost) throws SQLException
    {
        Long id = (Long)getSqlMapClient().insert("insert.activity.post", activityPost);
        return (id != null ? id : -1);
    }
}

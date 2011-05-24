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

import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;

public class ActivityPostDAOImpl extends ActivitiesSqlSessionDaoSupport implements ActivityPostDAO
{
    @SuppressWarnings("unchecked")
    public List<ActivityPostEntity> selectPosts(ActivityPostEntity activityPost) throws SQLException 
    {
        if ((activityPost.getJobTaskNode() != -1) &&
            (activityPost.getMinId() != -1) &&
            (activityPost.getMaxId() != -1) &&
            (activityPost.getStatus() != null))
        {
            return (List<ActivityPostEntity>)getSqlSession().selectList("alfresco.activities.select_activity_posts", activityPost);
        }
        else if (activityPost.getStatus() != null)
        {
            return (List<ActivityPostEntity>)getSqlSession().selectList("alfresco.activities.select_activity_posts_by_status_only", activityPost);
        }
        else
        {
            return new ArrayList<ActivityPostEntity>(0);
        }
    }

    public Long getMaxActivitySeq() throws SQLException 
    {
        return (Long)getSqlSession().selectOne("alfresco.activities.select_activity_post_max_seq");
    }
    
    public Long getMinActivitySeq() throws SQLException 
    {
        return (Long)getSqlSession().selectOne("alfresco.activities.select_activity_post_min_seq");
    }
    
    public Integer getMaxNodeHash() throws SQLException 
    {
        return (Integer)getSqlSession().selectOne("alfresco.activities.select_activity_post_max_jobtasknode");
    }

    public int updatePost(long id, String siteNetwork, String activityData, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity post = new ActivityPostEntity();
        post.setId(id);
        post.setSiteNetwork(siteNetwork);
        post.setActivityData(activityData);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return getSqlSession().update("alfresco.activities.update_activity_post_data", post);
    }
    
    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity post = new ActivityPostEntity();
        post.setId(id);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return getSqlSession().update("alfresco.activities.update_activity_post_status", post);
    }
    
    public int deletePosts(Date keepDate, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity params = new ActivityPostEntity();
        params.setPostDate(keepDate);
        params.setStatus(status.toString());
        
        return getSqlSession().delete("alfresco.activities.delete_activity_posts_older_than_date", params);
    }
    
    public long insertPost(ActivityPostEntity activityPost) throws SQLException
    {
        getSqlSession().insert("alfresco.activities.insert.insert_activity_post", activityPost);
        Long id = activityPost.getId();
        return (id != null ? id : -1);
    }
}

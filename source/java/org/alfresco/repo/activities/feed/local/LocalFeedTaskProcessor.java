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
package org.alfresco.repo.activities.feed.local;

import java.sql.SQLException;
import java.util.List;

import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.domain.activities.FeedControlDAO;
import org.alfresco.repo.domain.activities.FeedControlEntity;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * The local (ie. not grid) feed task processor is responsible for processing the individual feed job
 */
public class LocalFeedTaskProcessor extends FeedTaskProcessor
{
    private ActivityPostDAO postDAO;
    private ActivityFeedDAO feedDAO;
    private FeedControlDAO feedControlDAO;
    
    // used to start/end/commit transaction
    // note: currently assumes that all dao services are configured with this mapper / data source
    private SqlMapClient sqlMapper;

    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }
    
    public void setFeedDAO(ActivityFeedDAO feedDAO)
    {
        this.feedDAO = feedDAO;
    }
    
    public void setFeedControlDAO(FeedControlDAO feedControlDAO)
    {
        this.feedControlDAO = feedControlDAO;
    }
    
    public void setSqlMapClient(SqlMapClient sqlMapper)
    {
        this.sqlMapper = sqlMapper;
    }
    
    public void startTransaction() throws SQLException
    {
        sqlMapper.startTransaction();
    }
    
    public void commitTransaction() throws SQLException
    {
        sqlMapper.commitTransaction();
    }
    
    public void endTransaction() throws SQLException
    {
        sqlMapper.endTransaction();
    }
    
    public List<ActivityPostEntity> selectPosts(ActivityPostEntity selector) throws SQLException
    {
        return postDAO.selectPosts(selector);
    }
    
    public long insertFeedEntry(ActivityFeedEntity feed) throws SQLException
    {
        return feedDAO.insertFeedEntry(feed);
    }
    
    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException
    {
        return postDAO.updatePostStatus(id, status);
    }
    
    public List<FeedControlEntity> selectUserFeedControls(String userId) throws SQLException
    {
       return feedControlDAO.selectFeedControls(userId);
    }
}

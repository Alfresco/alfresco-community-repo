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
package org.alfresco.repo.domain.activities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Interface for activity post DAO service
 * 
 * @author janv
 * @since 3.0
 */
public interface ActivityPostDAO extends ActivitiesDAO
{
    public static final int MAX_LEN_USER_ID = 255;         // needs to match schema: feed_user_id, post_user_id
    public static final int MAX_LEN_SITE_ID = 255;         // needs to match schema: site_network
    public static final int MAX_LEN_ACTIVITY_TYPE = 255;   // needs to match schema: activity_type
    public static final int MAX_LEN_ACTIVITY_DATA = 1024;  // needs to match schema: activity_data
    public static final int MAX_LEN_APP_TOOL_ID = 36;      // needs to match schema: app_tool
    
    public static final int MAX_LEN_NAME = 255;            // eg. filename

    public List<ActivityPostEntity> selectPosts(ActivityPostEntity activityPost, int maxItems) throws SQLException;
    
    public Long getMaxActivitySeq() throws SQLException;
    
    public Long getMinActivitySeq() throws SQLException;
    
    public Integer getMaxNodeHash() throws SQLException;
    
    public int deletePosts(Date keepDate, ActivityPostEntity.STATUS status) throws SQLException;
    
    public long insertPost(ActivityPostEntity activityPost) throws SQLException;
    
    public int updatePost(long id, String network, String activityData, ActivityPostEntity.STATUS status) throws SQLException;
    
    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException;
}

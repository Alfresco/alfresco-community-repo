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
package org.alfresco.repo.domain.activities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Interface for activity post DAO service
 */
public interface ActivityPostDAO extends ActivitiesDAO
{
    public static final int MAX_LEN_USER_ID = 255;         // needs to match schema: feed_user_id, post_user_id
    public static final int MAX_LEN_SITE_ID = 255;         // needs to match schema: site_network
    public static final int MAX_LEN_ACTIVITY_TYPE = 255;   // needs to match schema: activity_type
    public static final int MAX_LEN_ACTIVITY_DATA = 4000;  // needs to match schema: activity_data
    public static final int MAX_LEN_APP_TOOL_ID = 36;      // needs to match schema: app_tool
    
    public List<ActivityPostEntity> selectPosts(ActivityPostEntity activityPost) throws SQLException;
    
    public Long getMaxActivitySeq() throws SQLException;
    
    public Long getMinActivitySeq() throws SQLException;
    
    public Integer getMaxNodeHash() throws SQLException;
    
    public int deletePosts(Date keepDate, ActivityPostEntity.STATUS status) throws SQLException;
    
    public long insertPost(ActivityPostEntity activityPost) throws SQLException;
    
    public int updatePost(long id, String network, String activityData, ActivityPostEntity.STATUS status) throws SQLException;
    
    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException;
}

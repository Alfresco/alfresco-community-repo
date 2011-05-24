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
import java.util.List;

import org.alfresco.repo.domain.activities.FeedControlDAO;
import org.alfresco.repo.domain.activities.FeedControlEntity;

public class FeedControlDAOImpl extends ActivitiesDAOImpl implements FeedControlDAO
{
    public long insertFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        template.insert("alfresco.activities.insert.insert_activity_feedcontrol", activityFeedControl);
        Long id = activityFeedControl.getId();
        return (id != null ? id : -1);
    }
    
    public int deleteFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        return template.delete("alfresco.activities.delete_activity_feedcontrol", activityFeedControl);
    }
    
    @SuppressWarnings("unchecked")
    public List<FeedControlEntity> selectFeedControls(String feedUserId) throws SQLException
    {
        FeedControlEntity params = new FeedControlEntity(feedUserId);

        return (List<FeedControlEntity>)template.selectList("alfresco.activities.select_activity_feedcontrols_for_user", params);
    }
    
    public long selectFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        Long id = (Long)template.selectOne("alfresco.activities.select_activity_feedcontrol", activityFeedControl);
        return (id != null ? id : -1);
    }
}

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

        return template.selectList("alfresco.activities.select_activity_feedcontrols_for_user", params);
    }
    
    public long selectFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        Long id = template.selectOne("alfresco.activities.select_activity_feedcontrol", activityFeedControl);
        return (id != null ? id : -1);
    }
}

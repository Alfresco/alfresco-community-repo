package org.alfresco.repo.domain.activities;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for user activity feed controls DAO service
 */
public interface FeedControlDAO
{
    public long insertFeedControl(FeedControlEntity activityFeedControl) throws SQLException;
    
    public int deleteFeedControl(FeedControlEntity activityFeedControl) throws SQLException;
    
    public List<FeedControlEntity> selectFeedControls(String userId) throws SQLException;
    
    public long selectFeedControl(FeedControlEntity activityFeedControl) throws SQLException;
}

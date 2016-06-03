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

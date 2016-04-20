package org.alfresco.repo.domain.activities;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;

/**
 * Interface for activity feed DAO service
 */
public interface ActivityFeedDAO extends ActivitiesDAO
{
    public static final int MAX_LEN_USER_ID = 255;            // needs to match schema: feed_user_id, post_user_id
    public static final int MAX_LEN_SITE_ID = 255;            // needs to match schema: site_network
    public static final int MAX_LEN_ACTIVITY_TYPE = 255;      // needs to match schema: activity_type
    public static final int MAX_LEN_ACTIVITY_SUMMARY = 4000;  // needs to match schema: activity_summary
    public static final int MAX_LEN_APP_TOOL_ID = 36;         // needs to match schema: app_tool
    
    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException;
    
    public int deleteFeedEntries(Integer maxIdRange) throws SQLException;
    public int deleteFeedEntries(Date keepDate) throws SQLException;
    
    public int deleteUserFeedEntries(String feedUserId, Date keepDate) throws SQLException;
    
    public int deleteUserFeedEntries(String feedUserId) throws SQLException;
    
    public int deleteSiteFeedEntries(String siteId, Date keepDate) throws SQLException;
    
    public int deleteSiteFeedEntries(String siteUserId) throws SQLException;
    
    public List<ActivityFeedEntity> selectSiteFeedsToClean(int maxFeedSize) throws SQLException;
    public List<ActivityFeedEntity> selectUserFeedsToClean(int maxFeedSize) throws SQLException;

    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedItems) throws SQLException;

    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteUserId, int maxFeedItems) throws SQLException;
    
    public PagingResults<ActivityFeedEntity> selectPagedUserFeedEntries(String feedUserId, String networkId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, PagingRequest pagingRequest) throws SQLException;
    
    public Long countSiteFeedEntries(String siteId, int maxFeedSize) throws SQLException;
    
    public Long countUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedSize) throws SQLException;
}

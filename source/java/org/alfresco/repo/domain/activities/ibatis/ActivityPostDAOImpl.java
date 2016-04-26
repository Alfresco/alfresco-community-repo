package org.alfresco.repo.domain.activities.ibatis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.apache.ibatis.session.RowBounds;

/**
 * @author janv
 * @since 3.0
 */
public class ActivityPostDAOImpl extends ActivitiesDAOImpl implements ActivityPostDAO
{
    @SuppressWarnings("unchecked")
    public List<ActivityPostEntity> selectPosts(ActivityPostEntity activityPost, int maxItems) throws SQLException 
    {
        int rowLimit = maxItems < 0 ? RowBounds.NO_ROW_LIMIT : maxItems;
        RowBounds rowBounds = new RowBounds(RowBounds.NO_ROW_OFFSET, rowLimit);
        
        if ((activityPost.getJobTaskNode() != -1) &&
            (activityPost.getMinId() != -1) &&
            (activityPost.getMaxId() != -1) &&
            (activityPost.getStatus() != null))
        {
            return template.selectList("alfresco.activities.select_activity_posts_by_params", activityPost, rowBounds);
        }
        else if (activityPost.getStatus() != null)
        {
            return template.selectList("alfresco.activities.select_activity_posts_by_status", activityPost, rowBounds);
        }
        else
        {
            return new ArrayList<ActivityPostEntity>(0);
        }
    }
    
    public Long getMaxActivitySeq() throws SQLException 
    {
        return template.selectOne("alfresco.activities.select_activity_post_max_seq");
    }
    
    public Long getMinActivitySeq() throws SQLException 
    {
        return template.selectOne("alfresco.activities.select_activity_post_min_seq");
    }
    
    public Integer getMaxNodeHash() throws SQLException 
    {
        return template.selectOne("alfresco.activities.select_activity_post_max_jobtasknode");
    }

    public int updatePost(long id, String siteNetwork, String activityData, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity post = new ActivityPostEntity();
        post.setId(id);
        post.setSiteNetwork(siteNetwork);
        post.setActivityData(activityData);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return template.update("alfresco.activities.update_activity_post_data", post);
    }
    
    public int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity post = new ActivityPostEntity();
        post.setId(id);
        post.setStatus(status.toString());
        post.setLastModified(new Date());
        
        return template.update("alfresco.activities.update_activity_post_status", post);
    }
    
    public int deletePosts(Date keepDate, ActivityPostEntity.STATUS status) throws SQLException
    {
        ActivityPostEntity params = new ActivityPostEntity();
        params.setPostDate(keepDate);
        params.setStatus(status.toString());
        
        return template.delete("alfresco.activities.delete_activity_posts_older_than_date", params);
    }
    
    public long insertPost(ActivityPostEntity activityPost) throws SQLException
    {
        template.insert("alfresco.activities.insert.insert_activity_post", activityPost);
        Long id = activityPost.getId();
        return (id != null ? id : -1);
    }
}

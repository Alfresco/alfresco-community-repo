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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityFeedQueryEntity;
import org.alfresco.util.Pair;
import org.apache.ibatis.session.RowBounds;

public class ActivityFeedDAOImpl extends ActivitiesDAOImpl implements ActivityFeedDAO
{
    @Override
    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException
    {
        // ALF-17455 temporary assertion that the format is "json" 
        // TODO remove the summary format completely.
        if(!activityFeed.getActivitySummaryFormat().equalsIgnoreCase("json"))
        {
            throw new AlfrescoRuntimeException("Obsolete summary format specified - only json expected");
        }
        
        template.insert("alfresco.activities.insert.insert_activity_feed", activityFeed);
        Long id = activityFeed.getId();
        return (id != null ? id : -1);
    }
    
    @Override
    public int deleteFeedEntries(Integer maxIdRange) throws SQLException
    {
        // Get the largest ID
        Long maxId = (Long) template.selectOne("alfresco.activities.select_activity_feed_entries_max_id");
        if (maxId == null)
        {
            return 0;       // This happens when there are no entries
        }
        Long minId = maxId - maxIdRange + 1;        // The delete leaves the ID we pass in
        if (minId <= 0)
        {
            return 0;
        }
        return template.delete("alfresco.activities.delete_activity_feed_entries_before_id", minId);
    }

    @Override
    public int deleteFeedEntries(Date keepDate) throws SQLException
    {
        return template.delete("alfresco.activities.delete_activity_feed_entries_older_than_date", keepDate);
    }
    
    @Override
    public int deleteSiteFeedEntries(String siteId) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        
        return template.delete("alfresco.activities.delete_activity_feed_for_site_entries", params);
    }
    
    @Override
    public int deleteSiteFeedEntries(String siteId, String format, Date keepDate) throws SQLException
    {
        
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return template.delete("alfresco.activities.delete_activity_feed_for_site_entries_older_than_date", params);
    }
    
    @Override
    public int deleteUserFeedEntries(String feedUserId, String format, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        params.setPostDate(keepDate);
        
        return template.delete("alfresco.activities.delete_activity_feed_for_feeduser_entries_older_than_date", params);
    }
    
    @Override
    public int deleteUserFeedEntries(String feedUserId) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
        
        return template.delete("alfresco.activities.delete_activity_feed_for_feeduser_entries", params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectUserFeedsToClean(int maxFeedSize) throws SQLException
    {
        return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_user_feeds_greater_than_max", maxFeedSize);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectSiteFeedsToClean(int maxFeedSize) throws SQLException
    {
        return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_site_feeds_greater_than_max", maxFeedSize);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
        if (minFeedId > -1)
        {
            params.setMinId(minFeedId);
        }
        
        int rowLimit = maxFeedSize < 0 ? RowBounds.NO_ROW_LIMIT : maxFeedSize;
        RowBounds rowBounds = new RowBounds(RowBounds.NO_ROW_OFFSET, rowLimit);
        
        if (siteId != null)
        {
            // given site
            params.setSiteNetwork(siteId);
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedEntity>(0);
            }
            if ((!excludeThisUser) && (!excludeOtherUsers))
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select.select_activity_feed_for_feeduser_and_site", params, rowBounds);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_others_and_site", params, rowBounds);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_me_and_site", params, rowBounds);
            }
        }
        else
        {
            // all sites
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new ArrayList<ActivityFeedEntity>(0);
            }
            if (!excludeThisUser && !excludeOtherUsers)
            {
                // no excludes => everyone => where feed user is me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser", params, rowBounds);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_others", params, rowBounds);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_me", params, rowBounds);
            }
        }
        
        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }

    private PagingResults<ActivityFeedEntity> getPagingResults(PagingRequest pagingRequest, final List<ActivityFeedEntity> feedEntries)
    {
        int maxItems = pagingRequest.getMaxItems();
    	final boolean hasMoreItems = feedEntries.size() > maxItems;
    	if(hasMoreItems)
    	{
    		feedEntries.remove(feedEntries.size() - 1);
    	}

    	return new PagingResults<ActivityFeedEntity>()
    	{
			@Override
			public List<ActivityFeedEntity> getPage()
			{
				return feedEntries;
			}

			@Override
			public boolean hasMoreItems() 
			{
				return hasMoreItems;
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount()
			{
				return new Pair<Integer, Integer>(null, null);
			}

			@Override
			public String getQueryExecutionId()
			{
				return null;
			}
    	};
    }
    
    @SuppressWarnings("unchecked")
    public PagingResults<ActivityFeedEntity> selectPagedUserFeedEntries(String feedUserId, String format, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, PagingRequest pagingRequest) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        params.setActivitySummaryFormat(format);
        
        if (minFeedId > -1)
        {
            params.setMinId(minFeedId);
        }

        int skipCount = pagingRequest.getSkipCount();
        int maxItems = pagingRequest.getMaxItems();

        RowBounds rowBounds = (maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ?
        		new RowBounds(skipCount, RowBounds.NO_ROW_LIMIT) :
        		new RowBounds(skipCount, maxItems + 1)); // +1 to check for more items

        if (siteId != null)
        {
            // given site
            params.setSiteNetwork(siteId);
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new EmptyPagingResults<ActivityFeedEntity>();
            }
            if ((!excludeThisUser) && (!excludeOtherUsers))
            {
                // no excludes => everyone => where feed user is me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser_and_site", params, rowBounds));
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser_others_and_site", params, rowBounds));
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser_me_and_site", params, rowBounds));
            }
        }
        else
        {
            // all sites
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return new EmptyPagingResults<ActivityFeedEntity>();
            }
            if (!excludeThisUser && !excludeOtherUsers)
            {
                // no excludes => everyone => where feed user is me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser", params, rowBounds));
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser_others", params, rowBounds));
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return getPagingResults(pagingRequest, (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select_activity_feed_for_feeduser_me", params, rowBounds));
            }
        }

        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }

    @SuppressWarnings("unchecked")
    public Long countSiteFeedEntries(String siteId, String format, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);

        // for given site
        return (Long)template.selectOne("alfresco.activities.count_activity_feed_for_site", params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteId, String format, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setSiteNetwork(siteId);
        params.setActivitySummaryFormat(format);
        
        int rowLimit = maxFeedSize < 0 ? RowBounds.NO_ROW_LIMIT : maxFeedSize;
        RowBounds rowBounds = new RowBounds(RowBounds.NO_ROW_OFFSET, rowLimit);
        
        // for given site
        return (List<ActivityFeedEntity>)template.selectList("alfresco.activities.select.select_activity_feed_for_site", params, rowBounds);
    }
}

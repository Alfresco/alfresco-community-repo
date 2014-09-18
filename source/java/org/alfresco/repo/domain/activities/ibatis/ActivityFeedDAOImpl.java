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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityFeedQueryEntity;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.Pair;
import org.apache.ibatis.session.RowBounds;

public class ActivityFeedDAOImpl extends ActivitiesDAOImpl implements ActivityFeedDAO
{
    private static final int DEFAULT_FETCH_BATCH_SIZE = 150;

    private TenantService tenantService;
    private int fetchBatchSize = DEFAULT_FETCH_BATCH_SIZE;
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setFetchBatchSize(int fetchBatchSize)
    {
        this.fetchBatchSize = fetchBatchSize;
    }

    public long insertFeedEntry(ActivityFeedEntity activityFeed) throws SQLException
    {
        template.insert("alfresco.activities.insert.insert_activity_feed", activityFeed);
        Long id = activityFeed.getId();
        return (id != null ? id : -1);
    }
    
    @Override
    public int deleteFeedEntries(Integer maxIdRange) throws SQLException
    {
        // Get the largest ID
        Long maxId = template.selectOne("alfresco.activities.select_activity_feed_entries_max_id");
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
    public int deleteSiteFeedEntries(String siteId, Date keepDate) throws SQLException
    {
        
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setSiteNetwork(siteId);
        params.setPostDate(keepDate);
        
        return template.delete("alfresco.activities.delete_activity_feed_for_site_entries_older_than_date", params);
    }
    
    @Override
    public int deleteUserFeedEntries(String feedUserId, Date keepDate) throws SQLException
    {
        ActivityFeedEntity params = new ActivityFeedEntity();
        params.setFeedUserId(feedUserId);
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
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setMaxFeedSize(maxFeedSize);
        
        return template.selectList("alfresco.activities.select_activity_user_feeds_greater_than_max", params);
    }

    @Override
    public Long countUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        
        if (minFeedId > -1)
        {
            params.setMinId(minFeedId);
        }
        
        if (siteId != null)
        {
            if (excludeThisUser && excludeOtherUsers)
            {
                return Long.valueOf(0);
            }
            if ((!excludeThisUser) && (!excludeOtherUsers))
            {
                // no excludes => everyone => where feed user is me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser_and_site", params);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser_others_and_site", params);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser_me_and_site", params);
            }
        }
        else
        {
            // all sites
            
            if (excludeThisUser && excludeOtherUsers)
            {
                // effectively NOOP - return empty feed
                return Long.valueOf(0);
            }
            if (!excludeThisUser && !excludeOtherUsers)
            {
                // no excludes => everyone => where feed user is me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser", params);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser_others", params);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return template.selectOne("alfresco.activities.count_activity_feed_for_feeduser_me", params);
            }
        }

        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectSiteFeedsToClean(int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setMaxFeedSize(maxFeedSize);
        
        return template.selectList("alfresco.activities.select_activity_site_feeds_greater_than_max", params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectUserFeedEntries(String feedUserId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        
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
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_and_site", params, rowBounds);
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_others_and_site", params, rowBounds);
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_me_and_site", params, rowBounds);
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
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser", params, rowBounds);
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_others", params, rowBounds);
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return template.selectList("alfresco.activities.select.select_activity_feed_for_feeduser_me", params, rowBounds);
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
    
    /*
     * Get a paged list of activities, filtering out those activities that do not belong to the network "networkId".
     */
    @SuppressWarnings("unchecked")
    private List<ActivityFeedEntity> filterByNetwork(String networkId, String siteId, String sql, ActivityFeedQueryEntity params, PagingRequest pagingRequest)
    {
        int expectedSkipCount = pagingRequest.getSkipCount();
        // +1 to calculate hasMoreItems
        int expectedMaxItems = (pagingRequest.getMaxItems() == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? pagingRequest.getMaxItems() : pagingRequest.getMaxItems() + 1);

        int skipCount = 0;
        int maxItems = fetchBatchSize;

        List<ActivityFeedEntity> ret = new LinkedList<ActivityFeedEntity>();

        int numMatchingItems = 0;
        int numAddedItems = 0;
        boolean skipping = true;

        List<ActivityFeedEntity> feedEntries = null;

        // fetch activities in batches of size "maxItems"
        // iterate through them, filtering out any that don't match the networkId
        do
        {
            RowBounds rowBounds = new RowBounds(skipCount, maxItems);

            feedEntries = template.selectList(sql, params, rowBounds);
            Iterator<ActivityFeedEntity> feedEntriesIt = feedEntries.iterator();

            while(feedEntriesIt.hasNext() && numAddedItems < expectedMaxItems)
            {
                ActivityFeedEntity activityFeedEntry = feedEntriesIt.next();
                
                if(siteId == null)
                {
                    // note: pending requirements for THOR-224, for now assume all activities are within context of site and filter by current tenant
                    if(!networkId.equals(tenantService.getDomain(activityFeedEntry.getSiteNetwork())))
                    {
                        continue;
                    }
                }

                numMatchingItems++;

                if(skipping)
                {
                    if(numMatchingItems > expectedSkipCount)
                    {
                        skipping = false;
                    }
                    else
                    {
                        continue;
                    }
                }

                ret.add(activityFeedEntry);
                
                numAddedItems++;
            }

            skipCount += feedEntries.size();
        }
        while(feedEntries != null && feedEntries.size() > 0 && numAddedItems < expectedMaxItems);

        return ret;
    }

    @Override
    public PagingResults<ActivityFeedEntity> selectPagedUserFeedEntries(String feedUserId, String networkId, String siteId, boolean excludeThisUser, boolean excludeOtherUsers, long minFeedId, PagingRequest pagingRequest) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setFeedUserId(feedUserId);
        
        if (minFeedId > -1)
        {
            params.setMinId(minFeedId);
        }

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
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser_and_site", params, pagingRequest));
            }
            else if ((excludeThisUser) && (!excludeOtherUsers))
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser_others_and_site", params, pagingRequest));
            }
            else if ((excludeOtherUsers) && (!excludeThisUser))
            {
                // exclude others => me => where feed user is me and post user is me
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser_me_and_site", params, pagingRequest));
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
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser", params, pagingRequest));
            }
            else if (excludeThisUser)
            {
                // exclude feed user => others => where feed user is me and post user is not me
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser_others", params, pagingRequest));
            }
            else if (excludeOtherUsers)
            {
                // exclude others => me => where feed user is me and post user is me
                return getPagingResults(pagingRequest, filterByNetwork(networkId, siteId, "alfresco.activities.select.select_activity_feed_for_feeduser_me", params, pagingRequest));
            }
        }

        // belts-and-braces
        throw new AlfrescoRuntimeException("Unexpected: invalid arguments");
    }

    @Override
    public Long countSiteFeedEntries(String siteId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setSiteNetwork(siteId);

        // for given site
        return template.selectOne("alfresco.activities.count_activity_feed_for_site", params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActivityFeedEntity> selectSiteFeedEntries(String siteId, int maxFeedSize) throws SQLException
    {
        ActivityFeedQueryEntity params = new ActivityFeedQueryEntity();
        params.setSiteNetwork(siteId);
        
        int rowLimit = maxFeedSize < 0 ? RowBounds.NO_ROW_LIMIT : maxFeedSize;
        RowBounds rowBounds = new RowBounds(RowBounds.NO_ROW_OFFSET, rowLimit);
        
        // for given site
        return template.selectList("alfresco.activities.select.select_activity_feed_for_site", params, rowBounds);
    }
}

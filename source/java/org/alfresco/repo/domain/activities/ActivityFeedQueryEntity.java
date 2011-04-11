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
package org.alfresco.repo.domain.activities;


/**
 * Entity bean to carry query parameters for <tt>alf_activity_feed</tt>
 *
 * @since 3.5
 */
public class ActivityFeedQueryEntity
{
    private Long minId;
    private String activitySummaryFormat;
    private String feedUserId;
    private String siteNetwork;
    
    public Long getMinId()
    {
        return minId;
    }
    
    public void setMinId(Long minId)
    {
        this.minId = minId;
    }
    
    public String getActivitySummaryFormat()
    {
        return activitySummaryFormat;
    }
    
    public void setActivitySummaryFormat(String activitySummaryFormat)
    {
        this.activitySummaryFormat = activitySummaryFormat;
    }
    
    public String getFeedUserId()
    {
        return feedUserId;
    }
    
    public void setFeedUserId(String feedUserId)
    {
        this.feedUserId = feedUserId;
    }
    
    public String getSiteNetwork()
    {
        return siteNetwork;
    }
    
    public void setSiteNetwork(String siteNetwork)
    {
        this.siteNetwork = siteNetwork;
    }
}

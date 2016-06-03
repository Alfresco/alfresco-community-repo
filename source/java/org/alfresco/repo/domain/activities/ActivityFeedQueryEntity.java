/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
    private Long maxId;
    private int maxFeedSize;
    private String feedUserId;
    private String siteNetwork;
    
    public String getNullValue()
    {
        return ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE;
    }
    
    public Long getMinId()
    {
        return minId;
    }
    
    public void setMinId(Long minId)
    {
        this.minId = minId;
    }

    public Long getMaxId()
    {
		return maxId;
	}

	public void setMaxId(Long maxId)
	{
		this.maxId = maxId;
	}

    public int getMaxFeedSize()
    {
        return maxFeedSize;
    }
    
    public void setMaxFeedSize(int maxFeedSize)
    {
        this.maxFeedSize = maxFeedSize;
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

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

import java.util.Date;

import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity Feed DAO
 */
public class ActivityFeedEntity
{ 
    private Long id; // internal DB-generated id
    private String activityType;
    private String activitySummary;
    private String activitySummaryFormat;
    private String feedUserId;
    private String postUserId;
    private String siteNetwork;
    private String appTool;
    private Date postDate;
    private Date feedDate; // for debug
    private long postId; // for debug - not an explicit FK constraint, could be used to implement re-generate
    

    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getActivitySummary()
    {
        return activitySummary;
    }
    
    public void setActivitySummary(String summary)
    {
        this.activitySummary = summary;
    }
    
    public String getFeedUserId()
    {
        return feedUserId;
    }
    
    public void setFeedUserId(String userid)
    {
        this.feedUserId = userid;
    }
    
    public String getPostUserId()
    {
        return postUserId;
    }
    
    public void setPostUserId(String userid)
    {
        this.postUserId = userid;
    }
    
    public String getActivitySummaryFormat()
    {
        return activitySummaryFormat;
    }
    
    public void setActivitySummaryFormat(String format)
    {
        this.activitySummaryFormat = format;
    }
    
    public String getSiteNetwork() 
    {
        return siteNetwork;
    }
    
    public void setSiteNetwork(String siteNetwork) 
    {
        this.siteNetwork = siteNetwork;
    }
	
    public String getActivityType()
    {
        return activityType;
    }
    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }
    
    public Date getPostDate()
    {
        return postDate;
    }

    public void setPostDate(Date postDate)
    {
        this.postDate = postDate;
    }
    
    public long getPostId()
    {
        return postId;
    }

    public void setPostId(long postId)
    {
        this.postId = postId;
    }
    
    public Date getFeedDate()
    {
        return feedDate;
    }

    public void setFeedDate(Date feedDate)
    {
        this.feedDate = feedDate;
    }

    public String getAppTool()
    {
        return appTool;
    }

    public void setAppTool(String appTool)
    {
        this.appTool = appTool;
    }
    
    public String getJSONString() throws JSONException
    {
        JSONObject jo = new JSONObject();
        
        jo.put("id", id);
        jo.put("postUserId", postUserId);
        jo.put("postDate", ISO8601DateFormat.format(postDate));
        if (feedUserId != null) { jo.put("feedUserId", feedUserId); } // eg. site feed
        jo.put("siteNetwork", siteNetwork);
        jo.put("activityType", activityType);
        jo.put("activitySummary", activitySummary);
        jo.put("activitySummaryFormat", activitySummaryFormat);
        
        return jo.toString();
    }
}

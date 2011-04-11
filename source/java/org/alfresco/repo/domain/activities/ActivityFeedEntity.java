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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.util.JSONtoFmModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Activity Feed DAO
 */
public class ActivityFeedEntity
{
    // JSON keys
    public static final String KEY_ACTIVITY_FEED_ID = "id";
    public static final String KEY_ACTIVITY_FEED_POST_DATE = "postDate";
    public static final String KEY_ACTIVITY_FEED_POST_USERID = "postUserId";
    public static final String KEY_ACTIVITY_FEED_USERID = "feedUserId";
    public static final String KEY_ACTIVITY_FEED_SITE = "siteNetwork";
    public static final String KEY_ACTIVITY_FEED_TYPE = "activityType";
    public static final String KEY_ACTIVITY_FEED_SUMMARY = "activitySummary";
    public static final String KEY_ACTIVITY_FEED_SUMMARY_FORMAT = "activitySummaryFormat";
    
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
        
        jo.put(KEY_ACTIVITY_FEED_ID, id);
        
        jo.put(KEY_ACTIVITY_FEED_POST_USERID, postUserId);
        jo.put(KEY_ACTIVITY_FEED_POST_DATE, ISO8601DateFormat.format(postDate));
        
        if (getFeedUserId() != null) { jo.put(KEY_ACTIVITY_FEED_USERID, getFeedUserId()); } // eg. site feed
        jo.put(KEY_ACTIVITY_FEED_SITE, siteNetwork);
        jo.put(KEY_ACTIVITY_FEED_TYPE, getActivityType());
        jo.put(KEY_ACTIVITY_FEED_SUMMARY, getActivitySummary());
        
        jo.put(KEY_ACTIVITY_FEED_SUMMARY_FORMAT, getActivitySummaryFormat());
        
        return jo.toString();
    }
    
    public Map<String, Object> getModel() throws JSONException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put(KEY_ACTIVITY_FEED_ID, id);
        
        map.put(KEY_ACTIVITY_FEED_POST_USERID, getPostUserId());
        map.put(KEY_ACTIVITY_FEED_POST_DATE, getPostDate());
        
        if (getFeedUserId() != null) { map.put(KEY_ACTIVITY_FEED_USERID, getFeedUserId()); } // eg. site feed
        map.put(KEY_ACTIVITY_FEED_SITE, getSiteNetwork());
        map.put(KEY_ACTIVITY_FEED_TYPE, getActivityType());
        
        map.put(KEY_ACTIVITY_FEED_SUMMARY_FORMAT, getActivitySummaryFormat());
        
        if ((getActivitySummary() != null) && getActivitySummaryFormat().equals(FeedTaskProcessor.FEED_FORMAT_JSON))
        {
            map.put(KEY_ACTIVITY_FEED_SUMMARY, JSONtoFmModel.convertJSONObjectToMap(getActivitySummary()));
        }
        else
        {
            map.put(KEY_ACTIVITY_FEED_SUMMARY, getActivitySummary());
        }
        
        return map;
    }
}

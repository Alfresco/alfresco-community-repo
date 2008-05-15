/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.activities.feed;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Activity Feed DAO
 */
public class ActivityFeedDAO
{ 
    private long id; // internal DB-generated id
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
    

    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
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
       return new JSONStringer()
           .object()
           .key("postUserId")
           .value(postUserId)
           .key("postDate")
           .value(postDate)
           .key("feedUserId")
           .value(feedUserId)
           .key("siteNetwork")
           .value(siteNetwork)
           .key("activityType")
           .value(activityType)
           .key("activitySummary")
           .value(activitySummary)
           .key("activitySummaryFormat")
           .value(activitySummaryFormat)
       .endObject().toString();
    }
}

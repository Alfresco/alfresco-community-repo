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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
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
    public static final String KEY_ACTIVITY_FEED_POST_USER_AVATAR_NODE = "postUserAvatar";
    public static final String KEY_ACTIVITY_FEED_USERID = "feedUserId";
    public static final String KEY_ACTIVITY_FEED_SITE = "siteNetwork";
    public static final String KEY_ACTIVITY_FEED_TYPE = "activityType";
    public static final String KEY_ACTIVITY_FEED_SUMMARY = "activitySummary";
    
    private Long id; // internal DB-generated id
    private String activityType;
    private String activitySummary;
    private String feedUserId = ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE;
    private String postUserId;
    private NodeRef postUserAvatarNodeRef;
    private String siteNetwork = ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE;
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
        if (userid != null && userid.length() > 0)
        {
            this.feedUserId = userid;
        }
    }
    
    public String getPostUserId()
    {
        return postUserId;
    }
    
    public void setPostUserId(String userid)
    {
        this.postUserId = userid;
    }
    
    public String getSiteNetwork() 
    {
        return siteNetwork;
    }
    
    public void setSiteNetwork(String siteNetwork) 
    {
        if (siteNetwork != null && siteNetwork.length() > 0)
        {
            this.siteNetwork = siteNetwork;
        }
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

    public NodeRef getPostUserAvatarNodeRef()
    {
        return postUserAvatarNodeRef;
    }

    public void setPostUserAvatarNodeRef(NodeRef postUserAvatarNodeRef)
    {
        this.postUserAvatarNodeRef = postUserAvatarNodeRef;
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
        if (postUserAvatarNodeRef != null)
        {
            jo.put(KEY_ACTIVITY_FEED_POST_USER_AVATAR_NODE, postUserAvatarNodeRef.toString());
        }
        jo.put(KEY_ACTIVITY_FEED_POST_DATE, ISO8601DateFormat.format(postDate));
        
        if (getFeedUserId() != null) { jo.put(KEY_ACTIVITY_FEED_USERID, getFeedUserId()); } // eg. site feed
        jo.put(KEY_ACTIVITY_FEED_SITE, siteNetwork);
        jo.put(KEY_ACTIVITY_FEED_TYPE, getActivityType());
        jo.put(KEY_ACTIVITY_FEED_SUMMARY, getActivitySummary());
        
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
        
        if (getActivitySummary() != null)
        {
            map.put(KEY_ACTIVITY_FEED_SUMMARY, JSONtoFmModel.convertJSONObjectToMap(getActivitySummary()));
        }
        else
        {
            map.put(KEY_ACTIVITY_FEED_SUMMARY, getActivitySummary());
        }
        
        return map;
    }
    
    // for debug only
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ActivityFeed\n[");
        sb.append("id=").append(id).append(",");
        sb.append("activityType=").append(activityType).append(",");
        sb.append("activitySummary=").append(activitySummary).append(",");
        sb.append("feedUserId=").append(feedUserId).append(",");
        sb.append("postUserId=").append(postUserId).append(",");
        sb.append("postDate=").append(postDate).append(",");
        sb.append("feedDate=").append(feedDate).append(",");
        sb.append("siteNetwork=").append(siteNetwork).append(",");
        sb.append("appTool=").append(appTool).append(",");
        sb.append("type=").append(activityType).append(",");
        sb.append("postId=").append(postId).append("\n]");
        return sb.toString();
    }
}

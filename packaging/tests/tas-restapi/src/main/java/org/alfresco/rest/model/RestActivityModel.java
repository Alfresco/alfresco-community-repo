/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * 
 * @author Cristina Axinte
 * 
 * Handles single Activity JSON responses
 * Example:
        {
          "postedAt": "2016-09-30T12:31:54.088+0000",
          "feedPersonId": "user-vtcaquckbq",
          "postPersonId": "user-vtcaquckbq",
          "siteId": "site-mNygPRuKka",
          "activitySummary": {
            "firstName": "User-vtCaqUCKBq FirstName",
            "lastName": "LN-User-vtCaqUCKBq",
            "memberFirstName": "User-vtCaqUCKBq FirstName",
            "role": "SiteManager",
            "memberLastName": "LN-User-vtCaqUCKBq",
            "title": "User-vtCaqUCKBq FirstName LN-User-vtCaqUCKBq (User-vtCaqUCKBq)",
            "memberPersonId": "User-vtCaqUCKBq"
          },
          "id": 14689,
          "activityType": "org.alfresco.site.user-joined"
        }
 *
 */
public class RestActivityModel extends TestModel implements IRestModel<RestActivityModel>
{
    @JsonProperty(value = "entry")
    RestActivityModel activityModel;
    
    @Override
    public RestActivityModel onModel()
    {
        return activityModel;
    }
    
    @JsonProperty(required = true)
    String id;
    
    /**
     * The id of the person who performed the activity
     */
    @JsonProperty(required = true)
    String postPersonId; 
    
    String siteId;
    String postedAt; 
    
    /**
     * The feed on which this activity was posted
     */
    @JsonProperty(required = true)
    String feedPersonId;
    
    RestActivitySummaryModel activitySummary;
    
    @JsonProperty(required = true)
    String activityType;
    
    public String getActivityType()
    {
        return activityType;
    }

    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getPostPersonId()
    {
        return postPersonId;
    }

    public void setPostPersonId(String postPersonId)
    {
        this.postPersonId = postPersonId;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public void setSiteID(String siteId)
    {
        this.siteId = siteId;
    }

    public String getPostedAt()
    {
        return postedAt;
    }

    public void setPostedAt(String postedAt)
    {
        this.postedAt = postedAt;
    }

    public String getFeedPersonId()
    {
        return feedPersonId;
    }

    public void setFeedPersonId(String feedPersonId)
    {
        this.feedPersonId = feedPersonId;
    }

    public RestActivitySummaryModel getActivitySummary()
    {
        return activitySummary;
    }

    public void setActivitySummary(RestActivitySummaryModel activitySummary)
    {
        this.activitySummary = activitySummary;
    }
}

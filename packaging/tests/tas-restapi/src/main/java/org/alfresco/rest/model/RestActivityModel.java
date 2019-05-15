package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Override
    public ModelAssertion<RestActivityModel> and() 
    {     
      return assertThat();
    }    
    
    @Override
    public ModelAssertion<RestActivityModel> assertThat() 
    {     
      return new ModelAssertion<RestActivityModel>(this);
    }    
     
}

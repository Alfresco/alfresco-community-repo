package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.UserModel;
import org.testng.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Cristina Axinte on 9/26/2016.
 * 
 * {
  "entry": {
    "id": "string",
    "homeNetwork": true,
    "isEnabled": true,
    "createdAt": "2016-09-26T11:33:36.343Z",
    "paidNetwork": true,
    "subscriptionLevel": "Free",
    "quotas": [
      {
        "id": "string",
        "limit": 0,
        "usage": 0
      }
    ]
  }
}
 * 
 */
public class RestNetworkModel extends RestPersonNetworkModel implements IRestModel<RestNetworkModel>
{
    @JsonProperty(value = "entry")
    RestNetworkModel model;
    
    @Override
    public RestNetworkModel onModel()
    {
       return model;
    }
    
    public RestNetworkModel assertNetworkHasName(UserModel user)
    {
        STEP(String.format("REST API: Assert that network has name '%s'", user.getDomain()));
        Assert.assertTrue(getId().equalsIgnoreCase(user.getDomain()), "Network doesn't have the expected name.");

        return this;
    }
    
    public RestNetworkModel assertNetworkIsEnabled()
    {
        STEP(String.format("REST API: Assert network is enabled"));
        Assert.assertEquals(isEnabled(), true, "Network should be enabled.");

        return this;
    }
    public RestNetworkModel assertNetworkIsNotEnabled()
    {
        STEP(String.format("REST API: Assert that network is disable"));
        Assert.assertEquals(isEnabled(), false, "Network should be disabled.");

        return this;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    @Override
    public ModelAssertion<RestNetworkModel> and() 
    {
      return assertThat();
    }
   
    @Override
    public ModelAssertion<RestNetworkModel> assertThat() 
    {
      return new ModelAssertion<RestNetworkModel>(this);
    }
}    
package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TagModel;
import org.testng.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles single Tag Entry JSON response
 * "entry":
 * {
 * "tag":"addedtag-c7444-1474370805346"
 * "id":"f45c4d06-f4df-42d7-a118-29121557d284"
 * }
 * 
 * @author Corina Nechifor
 */
public class RestTagModel extends TagModel implements IRestModel<RestTagModel>
{
    @JsonProperty(value = "entry")
    RestTagModel model;

    protected Integer count;

    public RestTagModel onModel()
    {
        return model;
    }

    public RestTagModel assertResponseIsNotEmpty()
    {
        STEP(String.format("REST API: Assert get tags response is not empty"));
        Assert.assertFalse(getId().isEmpty(), "Get tags response is empty.");

        return this;
    }

    /**
     * DSL for assertion on this rest model
     * @return
     */
    public ModelAssertion<RestTagModel> and() 
    {
      return new ModelAssertion<RestTagModel>(this);
    }
    
    /**
     * DSL for assertion on this rest model
     * @return
     */
    public ModelAssertion<RestTagModel> assertThat() 
    {
      return new ModelAssertion<RestTagModel>(this);
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

}    
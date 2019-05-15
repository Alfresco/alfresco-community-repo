package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestAggregateModel extends TestModel implements IRestModel<RestAggregateModel>
{
    @JsonProperty(value = "aggregate")
    RestAggregateModel model;
    
    @JsonProperty(required = true)
    private int numberOfRatings;
    private String average;

    @Override
    public RestAggregateModel onModel()
    {
        return model;
    }
    
    public int getNumberOfRatings()
    {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings)
    {
        this.numberOfRatings = numberOfRatings;
    }

    public String getAverage()
    {
        return average;
    }

    public void setAverage(String average)
    {
        this.average = average;
    }
    
    @Override
    public ModelAssertion<RestAggregateModel> and() 
    {      
      return assertThat();
    }   
    
    @Override
    public ModelAssertion<RestAggregateModel> assertThat() 
    {      
      return new ModelAssertion<RestAggregateModel>(this);
    }   
}
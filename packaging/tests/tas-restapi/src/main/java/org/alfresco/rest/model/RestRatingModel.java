package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestRatingModel extends TestModel implements IRestModel<RestRatingModel>
{
    @JsonProperty(value = "entry")
    RestRatingModel model;

    @JsonProperty(required = true)
    private String id;
    
    private String ratedAt;
    private String myRating;
    private RestAggregateModel aggregate;

    @Override
    public RestRatingModel onModel()
    {
        return model;
    }
    
    public RestAggregateModel getAggregate()
    {
        return aggregate;
    }

    public void setAggregate(RestAggregateModel aggregate)
    {
        this.aggregate = aggregate;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getRatedAt()
    {
        return ratedAt;
    }

    public void setRatedAt(String ratedAt)
    {
        this.ratedAt = ratedAt;
    }

    public String getMyRating()
    {
        return myRating;
    }

    public void setMyRating(String myRating)
    {
        this.myRating = myRating;
    }

    @Override
    public ModelAssertion<RestRatingModel> assertThat() 
    {      
      return new ModelAssertion<>(this);
    }

    @Override
    public ModelAssertion<RestRatingModel> and() 
    {      
      return assertThat();
    }
}    
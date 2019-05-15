package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated from 'Alfresco Authentication REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/authentication/versions/1}
 */
public class RestTicketModel extends TestModel implements IRestModel<RestTicketModel>
{
    @Override
    public ModelAssertion<RestTicketModel> assertThat()
    {
        return new ModelAssertion<RestTicketModel>(this);
    }

    @Override
    public ModelAssertion<RestTicketModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestTicketModel model;

    @Override
    public RestTicketModel onModel()
    {
        return model;
    }

    private String id;	    
    private String userId;	    

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }				

    public String getUserId()
    {
        return this.userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }				
}
 

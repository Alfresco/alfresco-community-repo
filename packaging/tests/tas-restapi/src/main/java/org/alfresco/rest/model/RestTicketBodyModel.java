package org.alfresco.rest.model;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated from 'Alfresco Authentication REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/authentication/versions/1}
 */
public class RestTicketBodyModel extends TestModel implements IRestModel<RestTicketBodyModel>
{
    @Override
    public ModelAssertion<RestTicketBodyModel> assertThat()
    {
        return new ModelAssertion<RestTicketBodyModel>(this);
    }

    @Override
    public ModelAssertion<RestTicketBodyModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestTicketBodyModel model;

    @Override
    public RestTicketBodyModel onModel()
    {
        return model;
    }

    private String userId;	    
    private String password;	    

    public String getUserId()
    {
        return this.userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }				

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }				
}
 

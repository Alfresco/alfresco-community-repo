package org.alfresco.rest.core;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

/**
 * Defines a Rest Response
 * 
 * @author Paul Brodner
 */
public class RestResponse
{
    private Response response;

    public RestResponse(Response response)
    {
        this.setResponse(response);
    }
  
    public String getStatusCode()
    {
        return String.valueOf(response.getStatusCode());
    }

    public Response getResponse()
    {
        return response;
    }

    public void setResponse(Response response)
    {
        this.response = response;
    }

    public <T> T toModel(Class<T> classz)
    {
        return response.as(classz);

    }

    public ValidatableResponse assertThat()
    {
        return response.then();
    }

}

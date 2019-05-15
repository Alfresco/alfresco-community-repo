package org.alfresco.rest.model;

import org.alfresco.rest.core.RestResponse;
import org.json.simple.JSONObject;

import com.google.gson.JsonObject;
import io.restassured.response.Response;

/**
 * Process RestReponse of type application/text
 * @author Meenal Bhave
 */
public class RestTextResponse extends RestResponse
{
    
    public RestTextResponse(Response response)
    {
        super(response);
    }

    public JsonObject getJsonObject()
    {
        return this.getResponse().jsonPath().get();
    }
    
    public String getJsonValueByPath(String path)
    {
        return this.getResponse().jsonPath().get(path).toString();
    }
    
    public JSONObject getJsonObjectByPath(String path)
    {        
        return new JSONObject(this.getResponse().jsonPath().get(path));
    }
}
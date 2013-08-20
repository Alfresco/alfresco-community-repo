package org.alfresco.rest.api.tests.client;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpResponse
{
	private HttpMethod method;
	private String user;
	private String response;
	private long time;
	
	public HttpResponse(HttpMethod method, String user, String response, long time)
	{
		super();
		this.method = method;
		this.user = user;
		this.time = time;
		this.response = response;
	}

	public int getStatusCode()
	{
		return method.getStatusCode();
	}

	public String getResponse()
	{
		return response;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		String requestType = null;
		String requestBody = null;
		if(method instanceof GetMethod)
		{
			requestType = "GET";
		}
		else if(method instanceof PutMethod)
		{
			requestType = "PUT";
			StringRequestEntity requestEntity = (StringRequestEntity)((PutMethod)method).getRequestEntity();
			if(requestEntity != null)
			{
				requestBody = requestEntity.getContent();
			}
		}
		else if(method instanceof PostMethod)
		{
			requestType = "POST";
			StringRequestEntity requestEntity = (StringRequestEntity)((PostMethod)method).getRequestEntity();
			if(requestEntity != null)
			{
				requestBody = requestEntity.getContent();
			}
		}
		else if(method instanceof DeleteMethod)
		{
			requestType = "DELETE";
		}

		try
		{
			sb.append(requestType + " request " + method.getURI());
			sb.append("\n");
		}
		catch (URIException e)
		{
		}
		sb.append(requestBody != null ? " \nbody = " + requestBody + "\n" : "");
		sb.append("user " + user);
		sb.append("\n");
		sb.append("returned " + method.getStatusCode() + " and took " + time + "ms");
		sb.append("\n");
		sb.append("Response content " + response);
		return sb.toString();
	}
	
	public JSONObject getJsonResponse()
	{
        JSONObject result = null;

        try
        {
            if(response != null && response instanceof String)
            {
                Object object = new JSONParser().parse((String)response);
                if(object instanceof JSONObject)
                {
                   return (JSONObject) object;
                }
            }
        }
        catch (ParseException error)
        {
            // Ignore errors, returning null
        }
       
        return result;
	}
}

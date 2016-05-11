/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.tests.client;

import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpResponse
{
	private HttpMethod method;
	private String user;
	private String response;
	private Map<String,String> headers;
	private long time;
	
	public HttpResponse(HttpMethod method, String user, String response, Map<String,String> headers, long time)
	{
		super();
		this.method = method;
		this.user = user;
		this.time = time;
		this.headers = headers;
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
	
	public Map<String,String> getHeaders()
	{
	    return headers;
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
        else if (method instanceof PutMethod)
        {
            requestType = "PUT";
            RequestEntity requestEntity = ((PutMethod) method).getRequestEntity();
            if (requestEntity instanceof StringRequestEntity)
            {
                StringRequestEntity stringRequestEntity = (StringRequestEntity) requestEntity;
                requestBody = stringRequestEntity.getContent();
            }
            else if (requestEntity != null)
            {
                requestBody = requestEntity.toString();
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

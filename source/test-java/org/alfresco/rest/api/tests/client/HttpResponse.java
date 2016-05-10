/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.rest.api.tests.client;

import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpResponse
{
	protected HttpMethod method;
	private String user;
	private byte[] responseBytes;
	private Map<String,String> headers;
	private long time;

	public HttpResponse(HttpMethod method, String user, byte[] responseBytes, Map<String,String> headers, long time)
	{
		this.method = method;
		this.user = user;
		this.time = time;
		this.headers = headers;
		this.responseBytes = responseBytes;
	}

	public int getStatusCode()
	{
		return method.getStatusCode();
	}

	public String getResponse()
	{
		if (responseBytes != null)
		{
			if (method instanceof HttpMethodBase)
			{
				// mimic method.getResponseBodyAsString
				return EncodingUtil.getString(responseBytes, ((HttpMethodBase)method).getResponseCharSet());
			}
			else
			{
				return new String(responseBytes);
			}
		}
		else
		{
			return null;
		}
	}

	public byte[] getResponseAsBytes()
	{
		return responseBytes;
	}
	
	public Map<String,String> getHeaders()
	{
	    return headers;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		String requestType = null;
        RequestEntity requestEntity = null;

		if (method instanceof GetMethod)
		{
			requestType = "GET";
		}
        else if (method instanceof PutMethod)
        {
            requestType = "PUT";
            requestEntity = ((PutMethod) method).getRequestEntity();
        }
        else if (method instanceof PostMethod)
		{
			requestType = "POST";
            requestEntity = ((PostMethod)method).getRequestEntity();
		}
		else if (method instanceof DeleteMethod)
		{
			requestType = "DELETE";
		}

		try
		{
			sb.append(requestType).append(" request ").append(method.getURI()).append("\n");
		}
		catch (URIException e)
		{
		}

        if (requestEntity != null)
        {
            sb.append("\nRequest body: ");
            if (requestEntity instanceof StringRequestEntity)
            {
                sb.append(((StringRequestEntity)requestEntity).getContent());
            }
            else if (requestEntity instanceof ByteArrayRequestEntity)
            {
                sb.append(" << ").append(((ByteArrayRequestEntity)requestEntity).getContent().length).append(" bytes >>");
            }
            sb.append("\n");
        }

		sb.append("user ").append(user).append("\n");
		sb.append("returned ").append(method.getStatusCode()).append(" and took ").append(time).append("ms").append("\n");

        String contentType = null;
        Header hdr = method.getResponseHeader("Content-Type");
        if (hdr != null)
        {
            contentType = hdr.getValue();
        }
        sb.append("Response content type: ").append(contentType).append("\n");

        if (contentType != null)
        {
            sb.append("\nResponse body: ");
            if (contentType.startsWith("text/plain") || contentType.startsWith("application/json"))
            {
                sb.append(getResponse());
                sb.append("\n");
            }
            else if(getResponseAsBytes() != null)
            {
                sb.append(" << ").append(getResponseAsBytes().length).append(" bytes >>");
                sb.append("\n");
            }
        }

		return sb.toString();
	}
	
	public JSONObject getJsonResponse()
	{
        JSONObject result = null;

        try
        {
			String response = getResponse();
            if (response != null)
            {
                Object object = new JSONParser().parse(response);
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

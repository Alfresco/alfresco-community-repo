/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.core;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.MissingFormatArgumentException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.restassured.RestAssured;
import org.springframework.http.HttpMethod;

/**
 * @author Paul Brodner
 */
public class RestRequest
{
    private static final String TOKEN_REGEX = "\\{.*?}";
    private String body;
    private HttpMethod httpMethod;
    private String path;
    private Object[] pathParams;
    private String contentType = "UTF-8";

    private RestRequest(HttpMethod httpMethod, String path, String... pathParams)
    {
        this(httpMethod, "", path, pathParams);
    }

    private RestRequest(HttpMethod httpMethod, String body, String path, String... pathParams)
    {
        setHttpMethod(httpMethod);
        setPath(path);
        setPathParams(pathParams);
        setBody(body);
        // Validate that the supplied path and pathParams are compatible.
        constructPath();
    }

    /**
     * Use this request when no body is needed
     * 
     * @param httpMethod
     * @param path
     * @param pathParams
     * @return
     */
    public static RestRequest simpleRequest(HttpMethod httpMethod, String path, String... pathParams)
    {       
        return new RestRequest(httpMethod, path, pathParams);
    }

    /**
     * Use this request when a body has to be provided
     * 
     * @param httpMethod
     * @param body
     * @param path
     * @param pathParams
     * @return
     */
    public static RestRequest requestWithBody(HttpMethod httpMethod, String body, String path, String... pathParams)
    {
        return new RestRequest(httpMethod, body, path, pathParams);
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
        addQueryParamsIfNeeded();
    }

    public Object[] getPathParams()
    {
        return pathParams;
    }

    public void setPathParams(Object[] pathParams)
    {
        this.pathParams = pathParams;
        addQueryParamsIfNeeded();
    }

    /**
     * Add query parameters to the path if needed.
     * <p>
     * e.g. For a path of "api/{fruit}" and params ["apple", "size=10", "colour=red"] then this will
     * update the path to be "api/{fruit}?{param0}&{param1}" so that the tokens will be populated by
     * RestAssured to make "api/apple?size=10&colour=red".
     */
    private void addQueryParamsIfNeeded()
    {
        // Don't do anything if the path or path params haven't been set yet.
        if (path == null || path.length() == 0 || pathParams == null)
        {
            return;
        }
        int groupCount = (int) Pattern.compile(TOKEN_REGEX).matcher(path).results().count();
        if (pathParams.length > groupCount)
        {
            // Add the remaining parameters to the URL query.
            String queryParams = IntStream.range(0, pathParams.length - groupCount)
                                          .mapToObj(index -> "{parameter" + index + "}")
                                          .collect(Collectors.joining("&"));
            path += (path.contains("?") ? "&" : "?") + queryParams;
        }
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * {@inheritDoc}
     * @throws MissingFormatArgumentException If there are not enough pathParams for the path.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder()
                    .append("Request: ")
                    .append(getHttpMethod())
                    .append(" ")
                    .append(RestAssured.baseURI)
                    .append(":")
                    .append(RestAssured.port)
                    .append("/")
                    .append(RestAssured.basePath)
                    .append("/");

        sb.append(constructPath());
                
        if(!getBody().isEmpty())
        {
            sb.append("\nbody:")
              .append(getBody());
        }
        sb.append("\n");
        
        
        return sb.toString();
    }

    /**
     * Populate the path with the pathParams.
     *
     * @return The path with tokens replaced with values.
     * @throws MissingFormatArgumentException If there are not enough pathParams for the path.
     */
    private String constructPath()
    {
        String getPathFormatted = getPath();
        if(getPath().contains("{"))
        {
            getPathFormatted = getPath().replaceAll(TOKEN_REGEX, "%s");
            getPathFormatted = String.format(getPathFormatted, getPathParams());
        }
        return getPathFormatted;
    }
}

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
package org.alfresco.rest.framework.core;

import java.util.List;

import org.springframework.http.HttpMethod;

/**
 * Operations that can typically take place on a Restful resource
 *
 * @author Gethin James
 */
public class ResourceOperation
{
    public static final int UNSET_STATUS = -1;
    private final HttpMethod httpMethod;
    private final String title;
    private final String description;
    private final List<ResourceParameter> parameters;
    private final int successStatus;
    
    /**
     * @param httpMethod HttpMethod
     * @param title String
     * @param description String
     * @param successStatus HTTP status
     */
    public ResourceOperation(HttpMethod httpMethod, String title, String description, List<ResourceParameter> parameters, int successStatus)
    {
        super();
        this.httpMethod = httpMethod;
        this.title = title;
        this.description = description;
        this.parameters = parameters;
        this.successStatus = successStatus;
    }

    public HttpMethod getHttpMethod()
    {
        return this.httpMethod;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public List<ResourceParameter> getParameters()
    {
        return this.parameters;
    }

    public int getSuccessStatus()
    {
        return successStatus;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceOperation [httpMethod=");
        builder.append(this.httpMethod);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", status=");
        builder.append(this.successStatus);
        builder.append(", description=");
        builder.append(this.description);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }
}

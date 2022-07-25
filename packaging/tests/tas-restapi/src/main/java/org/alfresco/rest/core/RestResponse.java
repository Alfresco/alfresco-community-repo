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

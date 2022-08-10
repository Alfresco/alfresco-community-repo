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
package org.alfresco.rest.requests;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.alfresco.rest.core.RestWrapper;

public abstract class ModelRequest<Request>
{
    protected RestWrapper restWrapper;

    public ModelRequest(RestWrapper restWrapper)
    {
        this.restWrapper = restWrapper;
    }

    @SuppressWarnings("unchecked")
    public Request usingParams(String... parameters)
    {
        restWrapper.withParams(parameters);
        return (Request) this;
    }

    /**
     * Use "include=path" in the URL query.
     * <p>
     * Nb. Replaces any existing parameters.
     */
    public Request includePath()
    {
        return include("path");
    }

    /**
     * Specify fields to include in the response.
     * <p>
     * Nb. Replaces any existing parameters.
     */
    @SuppressWarnings ("unchecked")
    public Request include(String... includes)
    {
        String includeString = Arrays.stream(includes).collect(Collectors.joining(","));
        restWrapper.withParams("include=" + includeString);
        return (Request) this;
    }
}

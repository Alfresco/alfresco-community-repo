/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.core;

import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RestIGCoreAPI;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Extends {@link RestWrapper} in order to call IG APIs with our own properties
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Service
@Scope(value = "prototype")
public class RMRestWrapper
{
    /** The class that wraps the ReST APIs from core. */
    @Autowired
    private RestWrapper restWrapper;
    @Autowired
    private RMRestProperties rmRestProperties;

    public RestIGCoreAPI withIGCoreAPI()
    {
        return new RestIGCoreAPI(this, rmRestProperties);
    }

    /** Get the core class that wraps the ReST APIs. */
    public RestWrapper getRestWrapper()
    {
        return restWrapper;
    }

    /** Authenticate specific user to Alfresco REST API */
    public void authenticateUser(UserModel userModel)
    {
        restWrapper.authenticateUser(userModel);
    }

    /** Process responses for a collection of models as {@link RestSiteModelsCollection}. */
    public FilePlanComponentsCollection processModels(Class<FilePlanComponentsCollection> classz,
                RestRequest simpleRequest)
    {
        try
        {
            return restWrapper.processModels(classz, simpleRequest);
        }
        catch (Exception e)
        {
            // TODO Hopefully remove this check when TAS stops using checked exceptions.
            // See https://gitlab.alfresco.com/tas/alfresco-tas-restapi-test/merge_requests/392
            throw new RuntimeException(e);
        }
    }

    /** Process responses for a single model as {@link RestSiteModel}. */
    public <T> T processModel(Class<T> classz, RestRequest restRequest)
    {
        try
        {
            return restWrapper.processModel(classz, restRequest);
        }
        catch (Exception e)
        {
            // TODO Hopefully remove this check when TAS stops using checked exceptions.
            // See https://gitlab.alfresco.com/tas/alfresco-tas-restapi-test/merge_requests/392
            throw new RuntimeException(e);
        }
    }

    /** Process a response that has no body - basically will need only the status code from it. */
    public void processEmptyModel(RestRequest simpleRequest)
    {
        try
        {
            restWrapper.processEmptyModel(simpleRequest);
        }
        catch (EmptyJsonResponseException e)
        {
            // TODO Hopefully remove this check when TAS stops using checked exceptions.
            // See https://gitlab.alfresco.com/tas/alfresco-tas-restapi-test/merge_requests/392
            throw new RuntimeException(e);
        }
    }

    /** Get the most recently returned status code. */
    public String getStatusCode()
    {
        return restWrapper.getStatusCode();
    }

    /** Set the status code. This should only be needed when calling APIs without using the TAS framework. */
    public void setStatusCode(String statusCode)
    {
        restWrapper.setStatusCode(statusCode);
    }

    /** Assert that a specific status code is returned. */
    public void assertStatusCodeIs(HttpStatus statusCode)
    {
        restWrapper.assertStatusCodeIs(statusCode);
    }

    /** @return A parameters string that you could pass on the request ?param=value */
    public String getParameters()
    {
        return restWrapper.getParameters();
    }

    /** Create a {@link UserModel} for a new test user. */
    public UserModel getTestUser()
    {
        return restWrapper.getTestUser();
    }
}

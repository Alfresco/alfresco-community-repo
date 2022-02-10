/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import io.restassured.builder.RequestSpecBuilder;

import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestHtmlResponse;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.rest.requests.coreAPI.RestCoreAPI;
import org.alfresco.rest.requests.search.SearchAPI;
import org.alfresco.rest.rm.community.requests.gscore.GSCoreAPI;
import org.alfresco.utility.model.StatusModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * Extends {@link RestWrapper} in order to call GS APIs with our own properties
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
    @Getter
    private RMRestProperties rmRestProperties;

    public GSCoreAPI withGSCoreAPI()
    {
        return new GSCoreAPI(this, getRmRestProperties());
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

    /** Get the last error thrown (if any). */
    public RestErrorModel assertLastError()
    {
        return restWrapper.assertLastError();
    }

    /** Process responses for a collection of models as {@link RestSiteModelsCollection}. */
    public <T> T processModels(Class<T> classz, RestRequest simpleRequest)
    {
        return restWrapper.processModels(classz, simpleRequest);
    }

    /** Process responses for a single model as {@link RestSiteModel}. */
    public <T> T processModel(Class<T> classz, RestRequest restRequest)
    {
        return restWrapper.processModel(classz, restRequest);
    }

    /** Process a response that has no body - basically will need only the status code from it. */
    public void processEmptyModel(RestRequest simpleRequest)
    {
        restWrapper.processEmptyModel(simpleRequest);
    }

    /** Get the most recently returned status object. */
    public StatusModel getLastStatus()
    {
        return restWrapper.getLastStatus();
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

    /** Get the Alfresco Core API. */
    public RestCoreAPI withCoreAPI()
    {
        return restWrapper.withCoreAPI();
    }

    /** Get the Alfresco Search API. */
    public SearchAPI withSearchAPI()
    {
        return restWrapper.withSearchAPI();
    }

    /**
     * You can handle the request sent to server by calling this method.
     * If for example you want to sent multipart form data you can use: <pre>
     * restClient.configureRequestSpec()
     *              .addMultiPart("filedata", Utility.getResourceTestDataFile("restapi-resource"))
     *              .addFormParam("renditions", "doclib")
     *              .addFormParam("autoRename", true);
     *
     * restClient.withCoreAPI().usingNode(ContentModel.my()).createNode();
     * </pre> This will create the node using the multipart data defined.
     */
    public RequestSpecBuilder configureRequestSpec()
    {
        return restWrapper.configureRequestSpec();
    }

    /**
     * Process a response that returns a html
     *
     * @throws EmptyJsonResponseException If there is no response from the server.
     */
    public RestHtmlResponse processHtmlResponse(RestRequest simpleRequest)
    {
        return restWrapper.processHtmlResponse(simpleRequest);
    }
}

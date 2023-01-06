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
package org.alfresco.rest.requests.cmisAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.utility.model.ContentModel;
import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

/**
 * @author Paul Brodner
 * 
 * Perform CMIS rest API calls "alfresco/api/-default-/public/cmis/versions/1.1/browser"
 * 
 * Take a look at {@link org.alfresco.rest.cmis.CmisBrowserTest}
 * @since 5.2.0-8
 */
public class RestCmisAPI extends ModelRequest<RestCmisAPI> {

    public RestCmisAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/cmis/versions/1.1/browser";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * @param objectCmisLocation
     * @return object
     */
    public RestResponse getRootObjectByLocation(ContentModel contentModel)
    {
        return getRootObjectByLocation(contentModel.getName());
    }

    /**
     * @param objectCmisLocation
     * @return object
     */
    public RestResponse getRootObjectByLocation(String objectName)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "root/Shared/{objectName}?{parameters}", objectName, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * @param objectId
     * @return object
     */
    public RestResponse getRootObjectByID(ContentModel contentModel)
    {
        return getRootObjectByID(contentModel.getNodeRef());
    }

    /**
     * @param objectId
     * @return object
     */
    public RestResponse getRootObjectByID(String objectID)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "root?objectid={objectId}?{parameters}", objectID, restWrapper.getParameters());
        return restWrapper.process(request);
    }

}

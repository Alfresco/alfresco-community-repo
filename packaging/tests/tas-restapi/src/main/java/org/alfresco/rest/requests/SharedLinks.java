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

import java.util.HashMap;

import javax.json.JsonArrayBuilder;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.rest.model.RestSharedLinksModel;
import org.alfresco.rest.model.RestSharedLinksModelCollection;
import org.alfresco.utility.model.FileModel;
import org.springframework.http.HttpMethod;

/**
 * Declares all Rest API under the /shared-links path
 * 
 * @author Meenal Bhave
 */
public class SharedLinks extends ModelRequest<SharedLinks>
{
    public SharedLinks(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * Retrieve sharedLinks using GET call on /shared-links
     * 
     * @return RestSharedLinksModelCollection
     * @throws JsonToModelConversionException
     */
    public RestSharedLinksModelCollection getSharedLinks()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSharedLinksModelCollection.class, request);
    }

    /**
     * Retrieve details for a specific sharedLink using GET call on "shared-links/{sharedLinkId}"
     * 
     * @param sharedLinksModel
     * @return RestSharedLinkModel
     * @throws JsonToModelConversionException
     */
    public RestSharedLinksModel getSharedLink(RestSharedLinksModel sharedLinksModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links/{sharedLinkId}?{parameters}", sharedLinksModel.getId(),
                restWrapper.getParameters());
        return restWrapper.processModel(RestSharedLinksModel.class, request);
    }

    /**
     * Retrieve content for a specific sharedLink using GET call on "shared-links/{sharedLinkId}/content"
     * 
     * @param sharedLinksModel
     * @return RestResponse
     */
    public RestResponse getSharedLinkContent(RestSharedLinksModel sharedLinksModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links/{sharedLinkId}/content?{parameters}", sharedLinksModel.getId(),
                restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Send email with a specific sharedLink using POST call on "shared-links/{sharedLinkId}/email"
     * 
     * @param sharedLinksModel
     * @param postBody
     * @return RestResponse
     */
    public RestResponse sendSharedLinkEmail(RestSharedLinksModel sharedLinksModel, String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "shared-links/{sharedLinkId}/email?{parameters}", sharedLinksModel.getId(),
                restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Retrieves Renditions for the specified sharedLink
     * 
     * @return RestRenditionInfoModelCollection
     * @throws JsonToModelConversionException
     */
    public RestRenditionInfoModelCollection getSharedLinkRenditions(RestSharedLinksModel sharedLinksModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links/{sharedLinkId}/renditions?{parameters}", sharedLinksModel.getId(),
                restWrapper.getParameters());
        return restWrapper.processModels(RestRenditionInfoModelCollection.class, request);
    }

    /**
     * Retrieves specific Rendition for the specified sharedLink
     * 
     * @param sharedLinksModel
     * @param renditionId
     * @return RestRenditionInfoModel
     * @throws JsonToModelConversionException
     */
    public RestRenditionInfoModel getSharedLinkRendition(RestSharedLinksModel sharedLinksModel, String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links/{sharedLinkId}/renditions/{renditionId}?{parameters}",
                sharedLinksModel.getId(), renditionId, restWrapper.getParameters());
        return restWrapper.processModel(RestRenditionInfoModel.class, request);
    }

    /**
     * Retrieve rendition content for the specified sharedLink using GET call on "shared-links/{sharedLinkId}/renditions/{renditionId}/content"
     * 
     * @param sharedLinksModel
     * @param renditionId
     * @return RestRenditionInfoModel
     */
    public RestResponse getSharedLinkRenditionContent(RestSharedLinksModel sharedLinksModel, String renditionId)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "shared-links/{sharedLinkId}/renditions/{renditionId}/content?{parameters}",
                sharedLinksModel.getId(), renditionId, restWrapper.getParameters());
        return restWrapper.process(request);
    }

    /**
     * Removes SharedLink for the specified file, the sharedlink is deleted, file is unshared as a result
     * 
     * @param RestSharedLinksModel
     * @return void
     */
    public void deleteSharedLink(RestSharedLinksModel sharedLinksModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "shared-links/{sharedLinkId}", sharedLinksModel.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Creates SharedLink for the specified file
     * 
     * @param file
     * @return RestSharedLinksModel
     */
    public RestSharedLinksModel createSharedLink(FileModel file)
    {
        String postBody = JsonBodyGenerator.keyValueJson("nodeId", file.getNodeRefWithoutVersion());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "shared-links?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestSharedLinksModel.class, request);
    }

    /**
     * Creates SharedLink for all the specified files
     * 
     * @param file list
     * @return RestSharedLinksModelCollection
     */
    public RestSharedLinksModelCollection createSharedLinks(FileModel... files)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();
        for (FileModel file : files)
        {
            array.add(JsonBodyGenerator.defineJSON().add("nodeId", file.getNodeRefWithoutVersion()));
        }
        String postBody = array.build().toString();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "shared-links?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSharedLinksModelCollection.class, request);

    }

    /**
     * Creates SharedLink for the specified file, with the given expiry date
     * 
     * @param file
     * @param expiryDate: format: "2027-03-23T23:00:00.000+0000";
     * @return RestSharedLinksModel
     */
    public RestSharedLinksModel createSharedLinkWithExpiryDate(FileModel file, String expiryDate)
    {
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("nodeId", file.getNodeRefWithoutVersion());
        body.put("expiresAt", expiryDate);
        String postBody = JsonBodyGenerator.keyValueJson(body);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "shared-links?{parameters}", restWrapper.getParameters());
        return restWrapper.processModel(RestSharedLinksModel.class, request);
    }
}

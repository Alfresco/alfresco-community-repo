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
package org.alfresco.rest.rm.community.requests.igCoreAPI;

import static com.jayway.restassured.RestAssured.given;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.POST;

import com.jayway.restassured.response.Response;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestHtmlResponse;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.RecordBodyFile;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Records  REST API Wrapper
 *
 *@author Rodica Sutu
 *@since 2.6
 */
@Component
@Scope (value = "prototype")
public class RecordsAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper
     */
    public RecordsAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Get the content for the electronic record
     *
     * @param recordId The id of the electronic record
     * @return The content for the given record id
     * @throws Exception for the following cases:
     * <ul>
     * <li>{@code recordId} has no content</li>
     * <li> {@code recordId} is not a valid format, or is not a record</li>
     * <li>authentication fails</li>
     * <li>{@code recordId} does not exist</li>
     * </ul>
     */
    public <T> T getRecordContentText(String recordId) throws Exception
    {
        mandatoryString("recordId", recordId);
        Response response = given().auth().basic(getRMRestWrapper().getTestUser().getUsername(),
                                                 getRMRestWrapper().getTestUser().getPassword()
                                                )
                                   .get("records/{recordId}/content", recordId)
                                   .andReturn();

        getRMRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));

        return (T) response.getBody().prettyPrint();
    }

    /**
     * Get the content RestHtmlResponse(Response header and body) for the electronic record
     *
     * @param recordId The id of the electronic record
     * @return The body and the header for the record
     * @throws Exception for the following cases:
     * <ul>
     * <li>{@code recordId} has no content</li>
     * <li> {@code recordId} is not a valid format, or is not a record</li>
     * <li>authentication fails</li>
     * <li>{@code recordId} does not exist</li>
     * </ul>
     */
    public RestHtmlResponse getRecordContent(String recordId) throws Exception
    {
        mandatoryString("recordId", recordId);
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "records/{recordId}/content", recordId);
        return getRMRestWrapper().processHtmlResponse(request);
    }

    /**
     * File the record recordId into file plan structure based on the location sent via the request body
     *
     * @param recordBodyFile The properties where to file the record
     * @param recordId       The id of the record to file
     * @return The {@link FilePlanComponent} with the given properties
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Invalid parameter: {@code recordBodyFile} is not a valid format,{@code recordId} is not a record</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to file to {@code fileplanComponentId}</li>
     *  <li>{@code recordId} does not exist</li>
     *  <li>targetParentId from recordBodyFile does not exist</li>
     *  <li>model integrity exception: the action breaks system's integrity restrictions</li>
     * </ul>
     *
     */
    public FilePlanComponent fileRecord(RecordBodyFile recordBodyFile, String recordId) throws Exception
    {
        mandatoryObject("recordBodyFile", recordBodyFile);
        mandatoryString("recordId", recordId);

        return fileRecord(recordBodyFile, recordId, EMPTY);
    }

    /**
     * File the record recordId into file plan structure based on the location sent via the request body
     *
     * @param recordBodyFile The properties where to file the record
     * @param recordId       The id of the record to file
     * @return The {@link FilePlanComponent} with the given properties
     * @throws Exception for the following cases:
     * <ul>
     *  <li>Invalid parameter: {@code recordBodyFile} is not a valid format,{@code recordId} is not a record</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to file to {@code fileplanComponentId}</li>
     *  <li>{@code recordId} does not exist</li>
     *  <li>targetParentId from recordBodyFile does not exist</li>
     *  <li>model integrity exception: the action breaks system's integrity restrictions</li>
     * </ul>
     *
     */
    public FilePlanComponent fileRecord(RecordBodyFile recordBodyFile, String recordId, String parameters) throws Exception
    {
        mandatoryObject("requestBodyFile", recordBodyFile);
        mandatoryString("recordId", recordId);

        return getRMRestWrapper().processModel(FilePlanComponent.class, requestWithBody(
            POST,
            toJson(recordBodyFile),
            "/records/{recordId}/file?{parameters}",
            recordId,
            parameters
        ));
    }

}


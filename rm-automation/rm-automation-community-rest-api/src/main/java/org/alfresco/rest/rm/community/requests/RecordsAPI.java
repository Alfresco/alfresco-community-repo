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
package org.alfresco.rest.rm.community.requests;

import static com.jayway.restassured.RestAssured.given;

import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;

import com.jayway.restassured.response.Response;

import org.alfresco.rest.core.RestAPI;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestHtmlResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 *Records  REST API Wrapper
 *
 *@author Rodica Sutu
 *@since 2.6
 */
@Component
@Scope (value = "prototype")
public class RecordsAPI extends RestAPI<FilePlanComponentAPI>
{
    public <T> T getRecordContentText(String recordId) throws Exception
    {
        mandatoryString("recordId", recordId);
        //RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "records/{recordId}/content?{parameters}", recordId, getParameters());
        Response response = given().auth().basic(usingRestWrapper().getTestUser().getUsername(), usingRestWrapper().getTestUser().getPassword())
            .get("records/{recordId}/content?{parameters}", recordId, getParameters())
            .andReturn();
        usingRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
        LOG.info("The record content is " + response.getBody().prettyPrint());
        return (T) response.getBody().prettyPrint();
    }

    public RestHtmlResponse getRecordContent(String recordId) throws Exception
    {
        mandatoryString("recordId", recordId);
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "records/{recordId}/content?{parameters}", recordId, getParameters());
        return usingRestWrapper().processHtmlResponse(request);
    }
}

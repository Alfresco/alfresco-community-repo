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

import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Records  REST API Wrapper
 *
 * @author Rodica Sutu
 * @since 2.6
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
     * @return {@link ResponseBody} representing content for the given record id
     * @throws Exception for the following cases:
     * <ul>
     * <li>{@code recordId} has no content</li>
     * <li> {@code recordId} is not a valid format, or is not a record</li>
     * <li>authentication fails</li>
     * <li>{@code recordId} does not exist</li>
     * </ul>
     */
    public ResponseBody<?> getRecordContent(String recordId) throws Exception
    {
        mandatoryString("recordId", recordId);
        Response response = given()
                                .auth().basic(getRMRestWrapper().getTestUser().getUsername(),
                                    getRMRestWrapper().getTestUser().getPassword())
                            .when()
                                   .get("records/{recordId}/content", recordId)
                                   .andReturn();
        getRMRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
        return response.getBody();
    }
}

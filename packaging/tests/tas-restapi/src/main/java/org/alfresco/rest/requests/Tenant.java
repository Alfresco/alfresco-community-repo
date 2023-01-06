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

import static io.restassured.RestAssured.given;
import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Declares all Rest API under the /alfresco/service/api/tenants path
 *
 */
public class Tenant extends ModelRequest<Tenant>
{
    private RestProperties restProperties;

    public Tenant(RestWrapper restWrapper, RestProperties restProperties)
    {
        super(restWrapper);
        this.restProperties = restProperties;
    }
    /**
     * Create tenant using POST call on "http://{server}:{port}/alfresco/service/api/tenants"
     * 
     * @param userModel
     * @return
     * @throws JsonToModelConversionException
     */
    public void createTenant(UserModel userModel)
    {
        STEP(String.format("DATAPREP: Create new tenant %s", userModel.getDomain()));
        String json = String.format("{\"tenantDomain\": \"%s\", \"tenantAdminPassword\": \"%s\"}", userModel.getDomain(), DataUser.PASSWORD);
        RequestSpecification request = given().auth().basic(restWrapper.getTestUser().getUsername(), restWrapper.getTestUser().getPassword())
                .contentType(ContentType.JSON);
        Response returnedResponse = request.contentType(ContentType.JSON).body(json)
                .post(String.format("%s/%s", restProperties.envProperty().getFullServerUrl(), "alfresco/service/api/tenants")).andReturn();
        if (!Integer.valueOf(returnedResponse.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            throw new IllegalStateException(String.format("Tenant is not created: %s", returnedResponse.asString()));
        }
    }
}

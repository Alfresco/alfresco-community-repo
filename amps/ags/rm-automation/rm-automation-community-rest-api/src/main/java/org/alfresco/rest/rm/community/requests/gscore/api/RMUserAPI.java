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
package org.alfresco.rest.rm.community.requests.gscore.api;

import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.JsonObject;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.RMRestProperties;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.utility.model.UserModel;

/**
 * RM user management API
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
// FIXME: As of December 2016 there is no v1-style API for managing RM users and users'
// roles/permissions. Until such APIs have become available, methods in this class are just proxies to
// "old-style" API calls.
public class RMUserAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public RMUserAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Helper method to obtain {@link AlfrescoHttpClient}
     * @return Initialized {@link AlfrescoHttpClient} instance
     */
    private AlfrescoHttpClient getAlfrescoHttpClient()
    {
        RMRestProperties properties = getRmRestWrapper().getRmRestProperties();

        AlfrescoHttpClientFactory factory = new AlfrescoHttpClientFactory();
        factory.setHost(properties.getServer());
        factory.setPort(Integer.parseInt(properties.getPort()));
        factory.setScheme(properties.getScheme());

        return factory.getObject();
    }

    /**
     * Assign RM role to user
     *
     * @param userName User's username
     * @param userRole User's RM role, one of {@link UserRoles} roles
     * @throws RuntimeException for failed requests
     */
    public void assignRoleToUser(String userName, String userRole)
    {
        UserModel adminUser = getRmRestWrapper().getTestUser();

        // get an "old-style" REST API client
        AlfrescoHttpClient client = getAlfrescoHttpClient();

        // override v1 baseURI and basePath
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(client.getApiUrl())
                .setBasePath("/")
                .build();

        Response response = given()
                .spec(spec)
                .log().all()
                .pathParam("role", userRole)
                .pathParam("authority", userName)
                .param("alf_ticket", client.getAlfTicket(adminUser.getUsername(),
                        adminUser.getPassword()))
                .when()
                .post("/rm/roles/{role}/authorities/{authority}")
                .prettyPeek()
                .andReturn();
        getRmRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
    }

    /**
     * Helper method to add permission on a component to user
     * @param filePlanComponentId The id of the file plan component on which permission should be given
     * @param user {@link UserModel} for a user to be granted permission
     * @param permission {@link UserPermissions} to be granted
     */
    public void addUserPermission(String filePlanComponentId, UserModel user, UserPermissions permission)
    {
        UserModel adminUser = getRmRestWrapper().getTestUser();

        // get an "old-style" REST API client
        AlfrescoHttpClient client = getAlfrescoHttpClient();

        JsonObject bodyJson = buildObject()
                .addArray("permissions")
                .addObject()
                .add("authority", user.getUsername())
                .add("role", permission.permissionId)
                .end()
                .getJson();

        // override v1 baseURI and basePath
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(client.getApiUrl())
                .setBasePath("/")
                .build();

        // execute an "old-style" API call
        Response response = given()
                .spec(spec)
                .auth().basic(adminUser.getUsername(), adminUser.getPassword())
                .contentType(ContentType.JSON)
                .body(bodyJson.toString())
                .pathParam("nodeId", filePlanComponentId)
                .log().all()
                .when()
                .post("/node/workspace/SpacesStore/{nodeId}/rmpermissions")
                .prettyPeek()
                .andReturn();
        getRmRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
    }

    /**
     * Helper method to set permission inheritance on a file plan component
     *
     * @param filePlanComponentId The id of the file plan component on which inherited permission should be set
     * @param isInherited          true if the permission is inherited
     *                             false if the permission inheritance is disabled
     */
    public void setUserPermissionInheritance(String filePlanComponentId, Boolean isInherited)
    {
        final UserModel adminUser = getRmRestWrapper().getTestUser();

        // get an "old-style" REST API client
        final AlfrescoHttpClient client = getAlfrescoHttpClient();

        final JsonObject bodyJson = buildObject()
                .addArray("permissions")
                .end()
                .add("isInherited", isInherited)
                .getJson();

        // override v1 baseURI and basePath
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(client.getApiUrl())
                .setBasePath("/")
                .build();

        // execute an "old-style" API call
        final Response response = given()
                .spec(spec)
                .auth().basic(adminUser.getUsername(), adminUser.getPassword())
                .contentType(ContentType.JSON)
                .body(bodyJson.toString())
                .pathParam("nodeId", filePlanComponentId)
                .log().all()
                .when()
                .post("/node/workspace/SpacesStore/{nodeId}/rmpermissions")
                .prettyPeek()
                .andReturn();
        getRmRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
    }


    /**
     * Creates a user with the given name using the old APIs
     *
     * @param userName The user name
     * @param userPassword The user's password
     * @param userEmail The user's e-mail address
     * @return <code>true</code> if the user was created successfully, <code>false</code> otherwise.
     */
    public boolean createUser(String userName, String userPassword, String userEmail)
    {
        UserModel adminUser = getRmRestWrapper().getTestUser();
        final AlfrescoHttpClient client = getAlfrescoHttpClient();

        JsonObject body = buildObject()
            .add("userName", userName)
            .add("firstName", userName)
            .add("lastName", userName)
            .add("password", userPassword)
            .add("email", userEmail)
            .getJson();

        final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(client.getApiUrl())
            .setBasePath("/")
            .setAuth(basic(adminUser.getUsername(), adminUser.getPassword()))
            .setContentType(ContentType.JSON)
            .setBody(body.toString())
            .build();

        // create POST request to "people" endpoint
        Response response = given()
            .spec(spec)
            .log().all()
        .when()
            .post("people")
            .prettyPeek()
            .andReturn();

        return (response.getStatusCode() == OK.value());
    }
}

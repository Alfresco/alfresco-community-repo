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

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.given;

import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.JsonObject;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.RMRestProperties;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.utility.model.UserModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * RM user management API
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
// FIXME: As of December 2016 there is no v1-style API for managing RM users and users'
// roles/permissions. Until such APIs have become available, methods in this class are just proxies to
// "old-style" API calls.
@Component
@Scope (value = "prototype")
public class RMUserAPI extends RMModelRequest
{
//    @Autowired
//    private UserService userService;
    
    private UserModel dataUser = new UserModel("admin", "admin");
    
    /**
     * @param rmRestWrapper
     */
    public RMUserAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    private AlfrescoHttpClient getAlfrescoHttpClient()
    {
        RMRestProperties properties = getRMRestWrapper().getRmRestProperties();
        AlfrescoHttpClientFactory factory = new AlfrescoHttpClientFactory();
        factory.setHost(properties.getServer());
        factory.setPort(Integer.parseInt(properties.getPort()));
        factory.setScheme(properties.getScheme());
        
        return factory.getObject();
    }
    
    public void assignRoleToUser(String userName, String userRole) throws Exception
    {
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
            .param("alf_ticket", client.getAlfTicket(dataUser.getUsername(), 
                dataUser.getPassword()))
        .when()
            .post("/rm/roles/{role}/authorities/{authority}")
            .prettyPeek()
            .andReturn();
        getRMRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
    }

    /**
     * Helper method to add permission on a component to user
     * @param component {@link FilePlanComponent} on which permission should be given
     * @param user {@link UserModel} for a user to be granted permission
     * @param permission {@link UserPermissions} to be granted
     */
    public void addUserPermission(FilePlanComponent component, UserModel user, String permission)
    {
        // get an "old-style" REST API client
        AlfrescoHttpClient client = getAlfrescoHttpClient();

        JsonObject bodyJson = buildObject()
            .addArray("permissions")
                .addObject()
                    .add("authority", user.getUsername())
                    .add("role", permission)
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
            .auth().basic(dataUser.getUsername(), dataUser.getPassword())
            .contentType(ContentType.JSON)
            .body(bodyJson.toString())
            .pathParam("nodeId", component.getId())
            .log().all()
        .when()
            .post("/node/workspace/SpacesStore/{nodeId}/rmpermissions")
            .prettyPeek()
            .andReturn();
        getRMRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));
    }

    /**
     * Creates a user with the given name using the old APIs
     *
     * @param userName The user name
     * @return <code>true</code> if the user was created successfully, <code>false</code> otherwise.
     */
    public boolean createUser(String userName)
    {
        AlfrescoHttpClient client = getAlfrescoHttpClient();
        
        JsonObject body = buildObject()
            .add("userName", userName)
            .add("firstName", userName)
            .add("lastName", userName)
            .add("password", TestData.DEFAULT_PASSWORD)
            .add("email", TestData.DEFAULT_EMAIL)
            .getJson();
        
        RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(client.getApiUrl())
            .setBasePath("/")
            .setAuth(basic(dataUser.getUsername(), dataUser.getPassword()))
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
        
        if (response.getStatusCode() == OK.value())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

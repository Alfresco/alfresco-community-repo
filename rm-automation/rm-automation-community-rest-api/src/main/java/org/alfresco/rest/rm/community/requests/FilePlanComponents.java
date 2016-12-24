/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.given;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.utility.model.UserModel;

/**
 * FIXME!!!
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponents extends RMModelRequest
{
    /**
     * @param restWrapper
     */
    public FilePlanComponents(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Get a file plan component
     *
     * @param filePlanComponentId The id of the file plan component to get
     * @return The {@link FilePlanComponentModel} for the given file plan component id
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     * </ul>
     */
    public FilePlanComponentModel getFilePlanComponent(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return getFilePlanComponent(filePlanComponentId, EMPTY);
    }

    /**
     * FIXME!!!
     *
     * @param filePlanComponentId FIXME!!!
     * @param parameters FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel getFilePlanComponent(String filePlanComponentId, String parameters) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return getRMRestWrapper().processModel(FilePlanComponentModel.class, simpleRequest(
                GET,
                "fileplan-components/{fileplanComponentId}?{parameters}",
                filePlanComponentId,
                parameters
        ));
    }

    /**
     * List child components of a file plan component
     *
     * @param filePlanComponentId The id of the file plan component of which to get child components
     * @return The {@link FilePlanComponent} for the given file plan component id
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     * </ul>
     */
    public FilePlanComponentsCollection listChildComponents(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return getRMRestWrapper().processModels(FilePlanComponentsCollection.class, simpleRequest(
                GET,
                "fileplan-components/{fileplanComponentId}/children",
                filePlanComponentId
        ));
    }

    /**
     * Creates a file plan component with the given properties under the parent node with the given id
     *
     * @param filePlanComponentModel The properties of the file plan component to be created
     * @param parentId The id of the parent where the new file plan component should be created
     * @return The {@link FilePlanComponentModel} with the given properties
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public FilePlanComponentModel createFilePlanComponent(FilePlanComponentModel filePlanComponentModel, String parentId) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponentModel);
        mandatoryString("parentId", parentId);

        return createFilePlanComponent(filePlanComponentModel, parentId, EMPTY);
    }

    /**
     * FIXME!!!
     *
     * @param filePlanComponentModel FIXME!!!
     * @param parentId FIXME!!!
     * @param parameters FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel createFilePlanComponent(FilePlanComponentModel filePlanComponentModel, String parentId, String parameters) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponentModel);
        mandatoryString("parentId", parentId);

        return getRMRestWrapper().processModel(FilePlanComponentModel.class, requestWithBody(
                POST,
                toJson(filePlanComponentModel),
                "fileplan-components/{fileplanComponentId}/children?{parameters}",
                parentId,
                parameters
                ));
    }

    /**
     * Create electronic record from file resource
     * @param electronicRecordModel {@link FilePlanComponent} for electronic record to be created
     * @param fileName the name of the resource file
     * @param parentId parent container id
     * @return newly created {@link FilePlanComponent}
     * @throws Exception if operation failed
     */
    public FilePlanComponentModel createElectronicRecord(FilePlanComponentModel electronicRecordModel, String fileName, String parentId) throws Exception
    {
        return createElectronicRecord(electronicRecordModel, new File(Resources.getResource(fileName).getFile()), parentId);
    }

    /**
     * Create electronic record from file resource
     * @param electronicRecordModel {@link FilePlanComponent} for electronic record to be created
     * @param recordContent {@link File} pointing to the content of the electronic record to be created
     * @param parentId parent container id
     * @return newly created {@link FilePlanComponent}
     * @throws Exception if operation failed
     */
    public FilePlanComponentModel createElectronicRecord(FilePlanComponentModel electronicRecordModel, File recordContent, String parentId) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", electronicRecordModel);
        mandatoryString("parentId", parentId);
        if (!electronicRecordModel.getNodeType().equals(CONTENT_TYPE))
        {
            fail("Only electronic records are supported");
        }

        UserModel currentUser = getRMRestWrapper().getTestUser();

        /*
         * For file uploads nodeBodyCreate is ignored hence can't be used. Append all FilePlanComponent fields
         * to the request.
         */
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setAuth(basic(currentUser.getUsername(), currentUser.getPassword()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(toJson(electronicRecordModel));

        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext())
        {
            String fieldName = fieldNames.next();
            builder.addMultiPart(fieldName, root.get(fieldName).asText(), ContentType.JSON.name());
        }

        builder.addMultiPart("filedata", recordContent, ContentType.BINARY.name());

        /*
         * RestWrapper adds some headers which break multipart/form-data uploads and also assumes json POST requests.
         * Upload the file using RestAssured library.
         */
        Response response = given()
            .spec(builder.build())
        .when()
            .post("fileplan-components/{fileplanComponentId}/children?{parameters}", parentId, getRMRestWrapper().getParameters())
            .andReturn();
        getRMRestWrapper().setStatusCode(Integer.toString(response.getStatusCode()));

        /* return a FilePlanComponent object representing Response */
        return response.jsonPath().getObject("entry", FilePlanComponentModel.class);
    }

    /**
     * Updates a file plan component
     *
     * @param filePlanComponentModel The properties to be updated
     * @param filePlanComponentId The id of the file plan component which will be updated
     * @param returns The updated {@link FilePlanComponent}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code fileplanComponentId} is not a valid format or {@code filePlanComponentProperties} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>the updated name clashes with an existing node in the current parent folder</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public FilePlanComponentModel updateFilePlanComponent(FilePlanComponentModel filePlanComponentModel, String filePlanComponentId) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponentModel);
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return updateFilePlanComponent(filePlanComponentModel, filePlanComponentId, EMPTY);
    }

    public FilePlanComponentModel updateFilePlanComponent(FilePlanComponentModel filePlanComponent, String filePlanComponentId, String parameters) throws Exception
    {
        mandatoryObject("filePlanComponentProperties", filePlanComponent);
        mandatoryString("filePlanComponentId", filePlanComponentId);

        return getRMRestWrapper().processModel(FilePlanComponentModel.class, requestWithBody(
                PUT,
                toJson(filePlanComponent),
                "fileplan-components/{fileplanComponentId}?{parameters}",
                filePlanComponentId,
                parameters
                ));
    }

    /**
     * Delete file plan component
     *
     * @param filePlanComponentId The id of the file plan component to be deleted
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code fileplanComponentId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code fileplanComponentId}</li>
     *  <li>{@code fileplanComponentId} does not exist</li>
     *  <li>{@code fileplanComponentId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteFilePlanComponent(String filePlanComponentId) throws Exception
    {
        mandatoryString("filePlanComponentId", filePlanComponentId);

        getRMRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "fileplan-components/{fileplanComponentId}",
                filePlanComponentId
        ));
    }
}

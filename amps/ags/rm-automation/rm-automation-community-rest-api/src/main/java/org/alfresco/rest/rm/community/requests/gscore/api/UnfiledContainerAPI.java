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

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.rest.rm.community.util.UnfiledContainerChildMixin;

/**
 * Unfiled Container REST API Wrapper
 *
 * @author Tuna Aksoy
 * @author Ana Bozianu
 * @since 2.6
 */
public class UnfiledContainerAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public UnfiledContainerAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * see {@link #getUnfiledContainer(String, String)}
     */
    public UnfiledContainer getUnfiledContainer(String unfiledContainerId)
    {
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getUnfiledContainer(unfiledContainerId, EMPTY);
    }

    /**
     * Gets an unfiled record container.
     *
     * @param unfiledContainerId The identifier of a unfiled record container
     * @param parameters The URL parameters to add
     * @return The {@link UnfiledContainer} for the given {@code unfiledContainerId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code unfiledContainerId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code unfiledContainerId}</li>
     *  <li>{@code unfiledContainerId} does not exist</li>
     * </ul>
     */
    public UnfiledContainer getUnfiledContainer(String unfiledContainerId, String parameters)
    {
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getRmRestWrapper().processModel(UnfiledContainer.class, simpleRequest(
                GET,
                "unfiled-containers/{unfiledContainerId}?{parameters}",
                unfiledContainerId,
                parameters
        ));
    }

    /**
     * see {@link #getUnfiledContainerChildren(String)} (String, String)}
     */
    public UnfiledContainerChildCollection getUnfiledContainerChildren(String unfiledContainerId)
    {
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getUnfiledContainerChildren(unfiledContainerId, EMPTY);
    }

    /**
     * Gets the children of an unfiled records container
     *
     * @param unfiledContainerId The identifier of an unfiled records container
     * @param parameters The URL parameters to add
     * @return The {@link UnfiledContainerChildCollection} for the given {@code unfiledContainerId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code unfiledContainerId}</li>
     *  <li>{@code unfiledContainerId} does not exist</li>
     *</ul>
     */
    public UnfiledContainerChildCollection getUnfiledContainerChildren(String unfiledContainerId, String parameters)
    {
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getRmRestWrapper().processModels(UnfiledContainerChildCollection.class, simpleRequest(
            GET,
            "unfiled-containers/{unfiledContainerId}/children?{parameters}",
            unfiledContainerId,
            parameters
        ));
    }

    /**
     * see {@link #createUnfiledContainerChild(UnfiledContainerChild, String, String)}
     */
    public UnfiledContainerChild createUnfiledContainerChild(UnfiledContainerChild unfiledContainerChildModel, String unfiledContainerId)
    {
        mandatoryObject("unfiledContainerChildModel", unfiledContainerChildModel);
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return createUnfiledContainerChild(unfiledContainerChildModel, unfiledContainerId, EMPTY);
    }

    /**
     * Creates an unfiled container child. Can be a record or an unfiled record folder.
     *
     * @param unfiledContainerChildModel The unfiled container child model which holds the information
     * @param unfiledContainerId The identifier of an unfiled container
     * @param parameters The URL parameters to add
     * @return The created {@link UnfiledContainerChild}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code unfiledContainerId} is not a valid format or {@code unfiledContainerChildModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code unfiledContainerId}</li>
     *  <li>{@code unfiledContainerId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public UnfiledContainerChild createUnfiledContainerChild(UnfiledContainerChild unfiledContainerChildModel, String unfiledContainerId, String parameters)
    {
        mandatoryObject("unfiledContainerChildModel", unfiledContainerChildModel);
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getRmRestWrapper().processModel(UnfiledContainerChild.class, requestWithBody(
                POST,
                toJson(unfiledContainerChildModel),
                "unfiled-containers/{unfiledContainerId}/children?{parameters}",
                unfiledContainerId,
                parameters
        ));
    }

    /**
     * Create a record from file resource
     *
     * @param unfiledContainerChildModel {@link UnfiledContainerChild} for electronic record to be created
     * @param unfiledContainerChildContent {@link File} pointing to the content of the electronic record to be created
     * @param unfiledContainerId The identifier of a unfiled container
     * @return newly created {@link UnfiledContainerChild}
     * @throws RuntimeException for invalid recordModel JSON strings
     */
    public UnfiledContainerChild uploadRecord(UnfiledContainerChild unfiledContainerChildModel, String unfiledContainerId, File unfiledContainerChildContent)
    {
        mandatoryObject("unfiledContainerChildModel", unfiledContainerChildModel);
        mandatoryObject("unfiledContainerChildContent", unfiledContainerChildContent);
        mandatoryString("unfiledContainerId", unfiledContainerId);

        if (!unfiledContainerChildModel.getNodeType().equals(CONTENT_TYPE))
        {
            fail("Only electronic records are supported");
        }

        /*
         * For file uploads nodeBodyCreate is ignored hence can't be used. Append all Record fields
         * to the request.
         */
        RequestSpecBuilder builder = getRmRestWrapper().configureRequestSpec();
        JsonNode root;
        try
        {
            root = new ObjectMapper().readTree(toJson(unfiledContainerChildModel, UnfiledContainerChild.class, UnfiledContainerChildMixin.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to convert model to JSON.", e);
        }
        // add request fields
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext())
        {
            String fieldName = fieldNames.next();
            builder.addMultiPart(fieldName, root.get(fieldName).asText(), ContentType.JSON.name());
        }
        builder.addMultiPart("filedata", unfiledContainerChildContent, ContentType.BINARY.name());

        // create node with given content
        return createUnfiledContainerChild(unfiledContainerChildModel, unfiledContainerId);
    }

    /**
     * see {@link #updateUnfiledContainer(UnfiledContainer, String, String)
     */
    public UnfiledContainer updateUnfiledContainer(UnfiledContainer unfiledContainerModel, String unfiledContainerId)
    {
        mandatoryObject("unfiledContainerModel", unfiledContainerModel);
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return updateUnfiledContainer(unfiledContainerModel, unfiledContainerId, EMPTY);
    }

    /**
     * Updates an unfiled record container
     *
     * @param unfiledContainerModel The unfiled record container model which holds the information
     * @param unfiledContainerId The identifier of an unfiled record container
     * @param parameters The URL parameters to add
     * @param returns The updated {@link UnfiledContainer}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code unfiledContainerId} is not a valid format or {@code unfiledContainerModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code unfiledContainerId}</li>
     *  <li>{@code unfiledContainerId} does not exist</li>
     *  <li>the updated name clashes with an existing root category of special container in the current fileplan</li>
     *  <li>model integrity exception, including file name with invalid characters</li>
     * </ul>
     */
    public UnfiledContainer updateUnfiledContainer(UnfiledContainer unfiledContainerModel, String unfiledContainerId, String parameters)
    {
        mandatoryObject("unfiledContainerModel", unfiledContainerModel);
        mandatoryString("unfiledContainerId", unfiledContainerId);

        return getRmRestWrapper().processModel(UnfiledContainer.class, requestWithBody(
                PUT,
                toJson(unfiledContainerModel),
                "unfiled-containers/{unfiledContainerId}?{parameters}",
                unfiledContainerId,
                parameters
        ));
    }

}

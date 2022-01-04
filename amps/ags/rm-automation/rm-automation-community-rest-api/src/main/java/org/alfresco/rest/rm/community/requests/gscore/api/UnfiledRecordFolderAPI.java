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
import static org.springframework.http.HttpMethod.DELETE;
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
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledRecordFolder;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.rest.rm.community.util.UnfiledContainerChildMixin;

/**
 * Unfiled Record Folders REST API Wrapper
 *
 * @author Ramona Popa
 * @since 2.6
 */
public class UnfiledRecordFolderAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public UnfiledRecordFolderAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * see {@link #getUnfiledRecordFolder(String, String)}
     */
    public UnfiledRecordFolder getUnfiledRecordFolder(String unfiledRecordFolderId)
    {
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getUnfiledRecordFolder(unfiledRecordFolderId, EMPTY);
    }

    /**
     * Gets an unfiled record folder.
     *
     * @param unfiledRecordFolderId The identifier of a unfiled record folder
     * @param parameters The URL parameters to add
     * @return The {@link UnfiledRecordFolder} for the given {@code unfiledRecordFolderId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code unfiledRecordFolderId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code unfiledRecordFolderId}</li>
     *  <li>{@code unfiledRecordFolderId} does not exist</li>
     * </ul>
     */
    public UnfiledRecordFolder getUnfiledRecordFolder(String unfiledRecordFolderId, String parameters)
    {
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getRmRestWrapper().processModel(UnfiledRecordFolder.class, simpleRequest(
                GET,
                "unfiled-record-folders/{unfiledRecordFolderId}?{parameters}",
                unfiledRecordFolderId,
                parameters
        ));
    }

    /**
     * see {@link #getUnfiledRecordFolderChildren(String, String)}
     */
    public UnfiledContainerChildCollection getUnfiledRecordFolderChildren(String unfiledRecordFolderId)
    {
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getUnfiledRecordFolderChildren(unfiledRecordFolderId, EMPTY);
    }

    /**
     * Gets the children of an unfiled record folder
     *
     * @param unfiledRecordFolderId The identifier of an unfiled records folder
     * @param parameters The URL parameters to add
     * @return The {@link UnfiledRecordFolderChildCollection} for the given {@code unfiledRecordFolderId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code unfiledRecordFolderId}</li>
     *  <li>{@code unfiledRecordFolderId} does not exist</li>
     *</ul>
     */
    public UnfiledContainerChildCollection getUnfiledRecordFolderChildren(String unfiledRecordFolderId, String parameters)
    {
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getRmRestWrapper().processModels(UnfiledContainerChildCollection.class, simpleRequest(
            GET,
            "unfiled-record-folders/{unfiledRecordFolderId}/children?{parameters}",
            unfiledRecordFolderId,
            parameters
        ));
    }

    /**
     * see {@link #createUnfiledRecordFolderChild(UnfiledContainerChild, String, String)}
     */
    public UnfiledContainerChild createUnfiledRecordFolderChild(UnfiledContainerChild unfiledRecordFolderChildModel, String unfiledRecordFolderId)
    {
        mandatoryObject("unfiledRecordFolderChildModel", unfiledRecordFolderChildModel);
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return createUnfiledRecordFolderChild(unfiledRecordFolderChildModel, unfiledRecordFolderId, EMPTY);
    }

    /**
     * Creates an unfiled record folder child. Can be a record or an unfiled record folder.
     *
     * @param unfiledRecordFolderChildModel The unfiled folder child model which holds the information
     * @param unfiledRecordFolderId The identifier of an unfiled folder
     * @param parameters The URL parameters to add
     * @return The created {@link UnfiledRecordFolderChild}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code unfiledRecordFolderId} is not a valid format or {@code unfiledRecordFolderChildModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code unfiledRecordFolderId}</li>
     *  <li>{@code unfiledRecordFolderId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public UnfiledContainerChild createUnfiledRecordFolderChild(UnfiledContainerChild unfiledRecordFolderChildModel, String unfiledRecordFolderId, String parameters)
    {
        mandatoryObject("unfiledRecordFolderChildModel", unfiledRecordFolderChildModel);
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getRmRestWrapper().processModel(UnfiledContainerChild.class, requestWithBody(
                POST,
                toJson(unfiledRecordFolderChildModel),
                "unfiled-record-folders/{unfiledRecordFolderId}/children?{parameters}",
                unfiledRecordFolderId,
                parameters
        ));
    }

    /**
     * Create a record from file resource
     *
     * @param unfiledRecordFolderChildModel {@link UnfiledContainerChild} for electronic record to be created
     * @param unfiledRecordFolderChildContent {@link File} pointing to the content of the electronic record to be created
     * @param unfiledRecordFolderId The identifier of a unfiled record folder
     * @return newly created {@link UnfiledContainerChild}
     * @throws RuntimeException for invalid recordModel JSON strings
     */
    public UnfiledContainerChild uploadRecord(UnfiledContainerChild unfiledRecordFolderChildModel, String unfiledRecordFolderId, File unfiledRecordFolderChildContent)
    {
        mandatoryObject("unfiledRecordFolderChildModel", unfiledRecordFolderChildModel);
        mandatoryObject("unfiledRecordFolderChildContent", unfiledRecordFolderChildContent);
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        if (!unfiledRecordFolderChildModel.getNodeType().equals(CONTENT_TYPE))
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
            root = new ObjectMapper().readTree(toJson(unfiledRecordFolderChildModel, UnfiledContainerChild.class, UnfiledContainerChildMixin.class));
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
        builder.addMultiPart("filedata", unfiledRecordFolderChildContent, ContentType.BINARY.name());

        // create node with given content
        return createUnfiledRecordFolderChild(unfiledRecordFolderChildModel, unfiledRecordFolderId);
    }

    /**
     * see {@link #updateUnfiledRecordFolder(UnfiledRecordFolder, String, String)
     */
    public UnfiledRecordFolder updateUnfiledRecordFolder(UnfiledRecordFolder unfiledRecordFolderModel, String unfiledRecordFolderId)
    {
        mandatoryObject("unfiledRecordFolderModel", unfiledRecordFolderModel);
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return updateUnfiledRecordFolder(unfiledRecordFolderModel, unfiledRecordFolderId, EMPTY);
    }

    /**
     * Updates an unfiled record folder
     *
     * @param unfiledRecordFolderModel The unfiled record folder model which holds the information
     * @param unfiledRecordFolderId The identifier of an unfiled record folder
     * @param parameters The URL parameters to add
     * @param returns The updated {@link UnfiledRecordFolder}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code unfiledRecordFolderId} is not a valid format or {@code unfiledRecordFolderModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code unfiledRecordFolderId}</li>
     *  <li>{@code unfiledRecordFolderId} does not exist</li>
     *  <li>the updated name clashes with an existing root category of special container in the current fileplan</li>
     *  <li>model integrity exception, including file name with invalid characters</li>
     * </ul>
     */
    public UnfiledRecordFolder updateUnfiledRecordFolder(UnfiledRecordFolder unfiledRecordFolderModel, String unfiledRecordFolderId, String parameters)
    {
        mandatoryObject("unfiledRecordFolderModel", unfiledRecordFolderModel);
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        return getRmRestWrapper().processModel(UnfiledRecordFolder.class, requestWithBody(
                PUT,
                toJson(unfiledRecordFolderModel),
                "unfiled-record-folders/{unfiledRecordFolderId}?{parameters}",
                unfiledRecordFolderId,
                parameters
        ));
    }

    /**
     * Deletes an unfiled record folder.
     *
     * @param unfiledRecordFolderId The identifier of a unfiled record folder
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code unfiledRecordFolderId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code unfiledRecordFolderId}</li>
     *  <li>{@code unfiledRecordFolderId} does not exist</li>
     *  <li>{@code unfiledRecordFolderId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteUnfiledRecordFolder(String unfiledRecordFolderId)
    {
        mandatoryString("unfiledRecordFolderId", unfiledRecordFolderId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "unfiled-record-folders/{recordFolderId}",
                unfiledRecordFolderId
        ));
    }

}

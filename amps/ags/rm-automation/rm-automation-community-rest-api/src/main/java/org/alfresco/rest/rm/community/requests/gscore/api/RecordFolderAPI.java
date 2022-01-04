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
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.alfresco.rest.rm.community.util.FilePlanComponentMixIn;

/**
 * Record folder REST API Wrapper
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RecordFolderAPI extends RMModelRequest
{
    /**
     * Constructor.
     *
     * @param rmRestWrapper RM REST Wrapper
     */
    public RecordFolderAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Deletes a record folder.
     *
     * @param recordFolderId The identifier of a record folder
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordFolderId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code recordFolderId}</li>
     *  <li>{@code recordFolderId} does not exist</li>
     *  <li>{@code recordFolderId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteRecordFolder(String recordFolderId)
    {
        mandatoryString("recordFolderId", recordFolderId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "record-folders/{recordFolderId}",
                recordFolderId
        ));
    }

    /**
     * see {@link #getRecordFolder(String, String)}
     */
    public RecordFolder getRecordFolder(String recordFolderId)
    {
        mandatoryString("recordFolderId", recordFolderId);

        return getRecordFolder(recordFolderId, EMPTY);
    }

    /**
     * Gets a record folder.
     *
     * @param recordFolderId The identifier of a record folder
     * @param parameters The URL parameters to add
     * @return The {@link RecordFolder} for the given {@code recordFolderId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordFolderId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordFolderId}</li>
     *  <li>{@code recordFolderId} does not exist</li>
     * </ul>
     */
    public RecordFolder getRecordFolder(String recordFolderId, String parameters)
    {
        mandatoryString("recordFolderId", recordFolderId);

        return getRmRestWrapper().processModel(RecordFolder.class, simpleRequest(
                GET,
                "record-folders/{recordFolderId}?{parameters}",
                recordFolderId,
                parameters
        ));
    }

    /**
     * see {@link #updateRecordFolder(RecordFolder, String, String)
     */
    public RecordFolder updateRecordFolder(RecordFolder recordFolderModel, String recordFolderId)
    {
        mandatoryObject("recordFolderModel", recordFolderModel);
        mandatoryString("recordFolderId", recordFolderId);

        return updateRecordFolder(recordFolderModel, recordFolderId, EMPTY);
    }

    /**
     * Updates a record folder.
     *
     * @param recordFolderModel The record folder model which holds the information
     * @param recordFolderId The identifier of a record folder
     * @param parameters The URL parameters to add
     * @param returns The updated {@link RecordFolder}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code recordFolderId} is not a valid format or {@code recordFolderModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code recordFolderId}</li>
     *  <li>{@code recordFolderId} does not exist</li>
     *  <li>the updated name clashes with an existing record folder in the current parent category</li>
     *  <li>model integrity exception, including file name with invalid characters</li>
     * </ul>
     */
    public RecordFolder updateRecordFolder(RecordFolder recordFolderModel, String recordFolderId, String parameters)
    {
        mandatoryObject("recordFolderModel", recordFolderModel);
        mandatoryString("recordFolderId", recordFolderId);

        return getRmRestWrapper().processModel(RecordFolder.class, requestWithBody(
                PUT,
                toJson(recordFolderModel),
                "record-folders/{recordFolderId}?{parameters}",
                recordFolderId,
                parameters
        ));
    }

    /**
     * see {@link #getRecordFolderChildren(String, String)}
     */
    public RecordFolderCollection getRecordFolderChildren(String recordFolderId)
    {
        mandatoryString("recordFolderId", recordFolderId);

        return getRecordFolderChildren(recordFolderId, EMPTY);
    }

    /**
     * Gets the children of a record folder.
     *
     * @param recordFolderId The identifier of a record folder
     * @param parameters The URL parameters to add
     * @return The {@link RecordFolderCollection} for the given {@code recordFolderId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordFolderId}</li>
     *  <li>{@code recordFolderId} does not exist</li>
     *</ul>
     */
    public RecordFolderCollection getRecordFolderChildren(String recordFolderId, String parameters)
    {
        mandatoryString("recordFolderId", recordFolderId);

        return getRmRestWrapper().processModels(RecordFolderCollection.class, simpleRequest(
            GET,
            "record-folders/{recordFolderId}/records?{parameters}",
            recordFolderId,
            parameters
        ));
    }

    /**
     * see {@link #createRecord(Record, String, String)}
     */
    public Record createRecord(Record recordModel, String recordFolderId)
    {
        mandatoryObject("recordModel", recordModel);
        mandatoryString("recordFolderId", recordFolderId);

        return createRecord(recordModel, recordFolderId, EMPTY);
    }

    /**
     * Create a record from file resource
     *
     * @param recordModel {@link Record} for electronic record to be created
     * @param recordContent {@link File} pointing to the content of the electronic record to be created
     * @param recordFolderId The identifier of a record folder
     * @return newly created {@link Record}
     * @throws RuntimeException for invalid recordModel JSON strings
     */
    public Record createRecord(Record recordModel, String recordFolderId, File recordContent) throws RuntimeException
    {
        mandatoryString("recordFolderId", recordFolderId);
        mandatoryObject("recordContent", recordContent);
        mandatoryObject("recordModel", recordModel);

        if (!recordModel.getNodeType().equals(CONTENT_TYPE))
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
            root = new ObjectMapper().readTree(toJson(recordModel, Record.class, FilePlanComponentMixIn.class));
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
        builder.addMultiPart("filedata", recordContent, ContentType.BINARY.name());

        // create node with given content
        return createRecord(recordModel, recordFolderId);
    }

    /**
     * Creates a record in a record folder child, i.e. a record.
     *
     * @param recordModel The record model which holds the information
     * @param recordFolderId The identifier of a record folder
     * @param parameters The URL parameters to add
     * @return The created {@link Record}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordFolderId is not a valid format or {@code recordModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code recordFolderId}</li>
     *  <li>{@code recordFolderId} does not exist</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public Record createRecord(Record recordModel, String recordFolderId, String parameters)
    {
        mandatoryObject("recordModel", recordModel);
        mandatoryString("recordFolderId", recordFolderId);

        return getRmRestWrapper().processModel(Record.class, requestWithBody(
                POST,
                toJson(recordModel),
                "record-folders/{recordFolderId}/records?{parameters}",
                recordFolderId,
                parameters
        ));
    }
}

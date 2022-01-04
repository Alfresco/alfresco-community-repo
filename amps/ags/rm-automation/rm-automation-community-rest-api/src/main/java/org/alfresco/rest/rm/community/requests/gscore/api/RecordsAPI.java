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
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;


import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

import io.restassured.response.ResponseBody;

/**
 * Records REST API Wrapper
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RecordsAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
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
     * @throws RuntimeException for the following cases:
     * <ul>
     * <li>{@code recordId} has no content</li>
     * <li> {@code recordId} is not a valid format, or is not a record</li>
     * <li>authentication fails</li>
     * <li>{@code recordId} does not exist</li>
     * </ul>
     */
    public ResponseBody<?> getRecordContent(String recordId)
    {
        mandatoryString("recordId", recordId);

        return getRmRestWrapper()
            .processHtmlResponse(simpleRequest(GET,"records/{recordId}/content", recordId))
            .getBody();

    }

    /**
     * File the record recordId into file plan structure based on the location sent via the request body
     *
     * @param recordBodyFile The properties where to file the record
     * @param recordId       The id of the record to file
     * @return The {@link Record} with the given properties
     * @throws RuntimeException for the following cases:
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
    public Record fileRecord(RecordBodyFile recordBodyFile, String recordId)
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
     * @return The {@link Record} with the given properties
     * @throws RuntimeException for the following cases:
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
    public Record fileRecord(RecordBodyFile recordBodyFile, String recordId, String parameters)
    {
        mandatoryObject("requestBodyFile", recordBodyFile);
        mandatoryString("recordId", recordId);

        return getRmRestWrapper().processModel(Record.class, requestWithBody(
            POST,
            toJson(recordBodyFile),
            "/records/{recordId}/file?{parameters}",
            recordId,
            parameters
        ));
    }

    /**
     * see {@link #completeRecord(String, String)
     */
    public Record completeRecord(String recordId)
    {
        mandatoryString("recordId", recordId);

        return completeRecord(recordId, EMPTY);
    }

    /**
     * Complete the record recordId
     *
     * @param recordId The id of the record to complete
     * @return The completed {@link Record} with the given properties
     * @throws RuntimeException for the following cases:
     *                   <ul>
     *                   <li>Invalid parameter: {@code recordId} is not a record</li>
     *                   <li>authentication fails</li>
     *                   <li>current user does not have permission to complete {@code recordId}</li>
     *                   <li>{@code recordId} does not exist or is frozen</li>
     *                   <li>model integrity exception: the record is already completed</li>
     *                   <li>model integrity exception: the record has missing meta-data</li>
     *                   </ul>
     */
    public Record completeRecord(String recordId, String parameters)
    {
        mandatoryString("recordId", recordId);

        return getRmRestWrapper().processModel(Record.class, simpleRequest(
            POST,
            "/records/{recordId}/complete?{parameters}",
            recordId,
            parameters
        ));
    }
    /**
     * Deletes a record.
     *
     * @param recordId The identifier of a record
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code recordId}</li>
     *  <li>{@code recordId} does not exist</li>
     *  <li>{@code recordId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteRecord(String recordId)
    {
        mandatoryString("recordId", recordId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "records/{recordId}",
                recordId
        ));
    }

    /**
     * see {@link #getRecord(String, String)}
     */
    public Record getRecord(String recordId)
    {
        mandatoryString("recordId", recordId);

        return getRecord(recordId, EMPTY);
    }

    /**
     * Gets a record.
     *
     * @param recordId The identifier of a record
     * @param parameters The URL parameters to add
     * @return The {@link Record} for the given {@code recordId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordId}</li>
     *  <li>{@code recordId} does not exist</li>
     * </ul>
     */
    public Record getRecord(String recordId, String parameters)
    {
        mandatoryString("recordId", recordId);

        return getRmRestWrapper().processModel(Record.class, simpleRequest(
                GET,
                "records/{recordId}?{parameters}",
                recordId,
                parameters
        ));
    }

    /**
     * see {@link #updateRecord(Record, String, String)
     */
    public Record updateRecord(Record recordModel, String recordId)
    {
        mandatoryObject("recordModel", recordModel);
        mandatoryString("recordId", recordId);

        return updateRecord(recordModel, recordId, EMPTY);
    }

    /**
     * Updates a record.
     *
     * @param recordModel The record model which holds the information
     * @param recordId The identifier of a record
     * @param parameters The URL parameters to add
     * @return The updated {@link Record}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code recordId} is not a valid format or {@code recordModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code recordId}</li>
     *  <li>{@code recordId} does not exist</li>
     *  <li>the updated name clashes with an existing record in the current parent folder</li>
     *  <li>model integrity exception, including file name with invalid characters</li>
     * </ul>
     */
    public Record updateRecord(Record recordModel, String recordId, String parameters)
    {
        mandatoryObject("recordModel", recordModel);
        mandatoryString("recordId", recordId);

        return getRmRestWrapper().processModel(Record.class, requestWithBody(
                PUT,
                toJson(recordModel),
                "records/{recordId}?{parameters}",
                recordId,
                parameters
        ));
    }
}

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
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * Record category REST API Wrapper
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RecordCategoryAPI extends RMModelRequest
{
    /**
     * Constructor.
     *
     * @param rmRestWrapper RM REST Wrapper
     */
    public RecordCategoryAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Deletes a record category.
     *
     * @param recordCategoryId The identifier of a record category
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordCategoryId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to delete {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *  <li>{@code recordCategoryId} is locked and cannot be deleted</li>
     * </ul>
     */
    public void deleteRecordCategory(String recordCategoryId)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
                DELETE,
                "record-categories/{recordCategoryId}",
                recordCategoryId
        ));
    }

    /**
     * see {@link #getRecordCategory(String, String)}
     */
    public RecordCategory getRecordCategory(String recordCategoryId)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRecordCategory(recordCategoryId, EMPTY);
    }

    /**
     * Gets a record category.
     *
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The {@link RecordCategory} for the given {@code recordCategoryId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordCategoryId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     * </ul>
     */
    public RecordCategory getRecordCategory(String recordCategoryId, String parameters)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRmRestWrapper().processModel(RecordCategory.class, simpleRequest(
                GET,
                "record-categories/{recordCategoryId}?{parameters}",
                recordCategoryId,
                parameters
        ));
    }

    /**
     * see {@link #updateRecordCategory(RecordCategory, String, String)
     */
    public RecordCategory updateRecordCategory(RecordCategory recordCategoryModel, String recordCategoryId)
    {
        mandatoryObject("recordCategoryModel", recordCategoryModel);
        mandatoryString("recordCategoryId", recordCategoryId);

        return updateRecordCategory(recordCategoryModel, recordCategoryId, EMPTY);
    }

    /**
     * Updates a record category.
     *
     * @param recordCategoryModel The record category model which holds the information
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @param returns The updated {@link RecordCategory}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code recordCategoryId} is not a valid format or {@code recordCategoryModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *  <li>the updated name clashes with an existing record category in the current parent category</li>
     *  <li>model integrity exception, including file name with invalid characters</li>
     * </ul>
     */
    public RecordCategory updateRecordCategory(RecordCategory recordCategoryModel, String recordCategoryId, String parameters)
    {
        mandatoryObject("recordCategoryModel", recordCategoryModel);
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRmRestWrapper().processModel(RecordCategory.class, requestWithBody(
                PUT,
                toJson(recordCategoryModel),
                "record-categories/{recordCategoryId}?{parameters}",
                recordCategoryId,
                parameters
        ));
    }

    /**
     * see {@link #getRecordCategoryChildren(String, String)}
     */
    public RecordCategoryChildCollection getRecordCategoryChildren(String recordCategoryId)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRecordCategoryChildren(recordCategoryId, EMPTY);
    }

    /**
     * Gets the children of a record category.
     *
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The {@link RecordCategoryChildCollection} for the given {@code recordCategoryId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *</ul>
     */
    public RecordCategoryChildCollection getRecordCategoryChildren(String recordCategoryId, String parameters)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRmRestWrapper().processModels(RecordCategoryChildCollection.class, simpleRequest(
            GET,
            "record-categories/{recordCategoryId}/children?{parameters}",
            recordCategoryId,
            parameters
        ));
    }

    /**
     * see {@link #createRecordCategoryChild(RecordCategoryChild, String, String)}
     */
    public RecordCategoryChild createRecordCategoryChild(RecordCategoryChild recordCategoryChildModel, String recordCategoryId)
    {
        mandatoryObject("recordCategoryChildModel", recordCategoryChildModel);
        mandatoryString("recordCategoryId", recordCategoryId);

        return createRecordCategoryChild(recordCategoryChildModel, recordCategoryId, EMPTY);
    }

    /**
     * Creates a record category child. Can be a record category or a record folder.
     *
     * @param recordCategoryChildModel The record category child model which holds the information
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The created {@link RecordCategoryChild}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordCategoryId} is not a valid format or {@code recordCategoryChildModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public RecordCategoryChild createRecordCategoryChild(RecordCategoryChild recordCategoryChildModel, String recordCategoryId, String parameters)
    {
        mandatoryObject("filePlanComponentProperties", recordCategoryChildModel);
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRmRestWrapper().processModel(RecordCategoryChild.class, requestWithBody(
                POST,
                toJson(recordCategoryChildModel),
                "record-categories/{recordCategoryId}/children?{parameters}",
                recordCategoryId,
                parameters
        ));
    }
}

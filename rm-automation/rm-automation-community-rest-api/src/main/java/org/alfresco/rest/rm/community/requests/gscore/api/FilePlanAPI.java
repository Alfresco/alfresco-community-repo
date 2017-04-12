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
package org.alfresco.rest.rm.community.requests.gscore.api;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * File plan REST API Wrapper
 *
 * @author Ramona Popa
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanAPI extends RMModelRequest
{
    /**
     * Constructor.
     *
     * @param restWrapper
     */
    public FilePlanAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * see {@link #getFilePlan(String, String)}
     */
    public FilePlan getFilePlan(String filePlanId)
    {
        mandatoryString("filePlanId", filePlanId);

        return getFilePlan(filePlanId, EMPTY);
    }

    /**
     * Gets a file plan.
     *
     * @param filePlanId The identifier of a file plan
     * @param parameters The URL parameters to add
     * @return The {@link FilePlan} for the given {@code filePlanId}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code filePlanId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code filePlanId}</li>
     *  <li>{@code filePlanId} does not exist</li>
     * </ul>
     */
    public FilePlan getFilePlan(String filePlanId, String parameters)
    {
        mandatoryString("filePlanId", filePlanId);

        return getRmRestWrapper().processModel(FilePlan.class, simpleRequest(
                GET,
                "/file-plans/{filePlanId}?{parameters}",
                filePlanId,
                parameters
        ));
    }

    /**
     * see {@link #getRootRecordCategories(String, String)}
     */
    public RecordCategoryCollection getRootRecordCategories(String filePlanId)
    {
        mandatoryString("filePlanId", filePlanId);

        return getRootRecordCategories(filePlanId, EMPTY);
    }

    /**
     * Gets the children (root categories) of a file plan.
     *
     * @param filePlanId The identifier of a file plan
     * @param parameters The URL parameters to add
     * @return The {@link RecordCategoryCollection} for the given {@code filePlanId}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code filePlanId}</li>
     *  <li>{@code filePlanId} does not exist</li>
     *</ul>
     */
    public RecordCategoryCollection getRootRecordCategories(String filePlanId, String parameters)
    {
        mandatoryString("filePlanId", filePlanId);

        return getRmRestWrapper().processModels(RecordCategoryCollection.class, simpleRequest(
            GET,
            "file-plans/{filePlanId}/categories?{parameters}",
            filePlanId,
            parameters
        ));
    }

    /**
     * see {@link #createRootRecordCategory(RecordCategory, String, String)}
     */
    public RecordCategory createRootRecordCategory(RecordCategory recordCategoryModel, String filePlanId) throws Exception
    {
        mandatoryObject("recordCategoryModel", recordCategoryModel);
        mandatoryString("filePlanId", filePlanId);

        return createRootRecordCategory(recordCategoryModel, filePlanId, EMPTY);
    }

    /**
     * Creates a root record category.
     *
     * @param recordCategoryModel The record category model which holds the information
     * @param filePlanId The identifier of a file plan
     * @param parameters The URL parameters to add
     * @return The created {@link RecordCategory}
     * @throws Exception for the following cases:
     * <ul>
     *  <li>{@code filePlanId} is not a valid format or {@code filePlanId} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code filePlanId}</li>
     *  <li>{@code filePlanIds} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     *  <li>model integrity exception, including node name with invalid characters</li>
     * </ul>
     */
    public RecordCategory createRootRecordCategory(RecordCategory recordCategoryModel, String filePlanId, String parameters) throws Exception
    {
        mandatoryObject("recordCategoryModel", recordCategoryModel);
        mandatoryString("filePlanId", filePlanId);

        return getRmRestWrapper().processModel(RecordCategory.class, requestWithBody(
                POST,
                toJson(recordCategoryModel),
                "file-plans/{filePlanId}/categories?{parameters}",
                filePlanId,
                parameters
        ));
    }
}

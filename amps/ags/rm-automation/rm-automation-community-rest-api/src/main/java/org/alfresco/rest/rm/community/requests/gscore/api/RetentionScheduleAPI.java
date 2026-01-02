/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionSchedule;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleActionDefinition;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleCollection;
import org.alfresco.rest.rm.community.model.retentionschedule.RetentionScheduleStepCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class RetentionScheduleAPI extends RMModelRequest
{

    /**
     * @param rmRestWrapper
     */
    public RetentionScheduleAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }


    /**
     * Creates a retention schedule.
     *
     * @param retentionScheduleModel The retentionSchedule model
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The created {@link RetentionSchedule}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code recordCategoryId} is not a valid format or {@code recordCategoryId} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     * </ul>
     */
    public RetentionSchedule createRetentionSchedule(RetentionSchedule retentionScheduleModel, String recordCategoryId, String parameters)
    {
        mandatoryString("recordCategoryId", recordCategoryId);
        mandatoryObject("retentionScheduleModel", retentionScheduleModel);

        return getRmRestWrapper().processModel(RetentionSchedule.class, requestWithBody(
            POST,
            toJson(retentionScheduleModel),
            "record-categories/{recordCategoryId}/retention-schedules",
            recordCategoryId,
            parameters
        ));
    }

    /**
     * See {@link #createRetentionSchedule(RetentionSchedule, String, String)}
     */
    public RetentionSchedule createRetentionSchedule(RetentionSchedule retentionScheduleModel, String recordCategoryId)
    {
        return createRetentionSchedule(retentionScheduleModel, recordCategoryId, EMPTY);
    }

    /**
     * Gets the retentionSchedule of a record category.
     *
     * @param recordCategoryId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The {@link RetentionSchedule} for the given {@code recordCategoryId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *</ul>
     */
    public RetentionScheduleCollection getRetentionSchedule(String recordCategoryId, String parameters)
    {
        mandatoryString("recordCategoryId", recordCategoryId);

        return getRmRestWrapper().processModels(RetentionScheduleCollection.class, simpleRequest(
            GET,
            "record-categories/{recordCategoryId}/retention-schedules?{parameters}",
            recordCategoryId,
            parameters
        ));
    }

    /**
     * See {@link #getRetentionSchedule(String, String)}
     */
    public RetentionScheduleCollection getRetentionSchedule(String recordCategoryId)
    {
        return getRetentionSchedule(recordCategoryId, EMPTY);
    }

    /**
     * Creates a step in the retention schedule.
     *
     * @param retentionScheduleActionDefinition The retentionScheduleActionDefinition model
     * @param retentionScheduleId The identifier of a retention schedule id
     * @param parameters The URL parameters to add
     * @return The created {@link RetentionScheduleActionDefinition}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code retentionScheduleId} is not a valid format or {@code retentionScheduleId} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to add children to {@code retentionScheduleId}</li>
     *  <li>{@code retentionScheduleId} does not exist</li>
     *  <li>new name clashes with an existing node in the current parent container</li>
     * </ul>
     */
    public RetentionScheduleActionDefinition createRetentionScheduleStep(RetentionScheduleActionDefinition retentionScheduleActionDefinition, String retentionScheduleId, String parameters)
    {
        mandatoryString("retentionScheduleId", retentionScheduleId);
        mandatoryObject("retentionScheduleActionDefinition", retentionScheduleActionDefinition);

        return getRmRestWrapper().processModel(RetentionScheduleActionDefinition.class, requestWithBody(
            POST,
            toJson(retentionScheduleActionDefinition),
            "retention-schedules/{retentionScheduleId}/retention-steps",
            retentionScheduleId,
            parameters
        ));
    }

    /**
     * See {@link #createRetentionScheduleStep(RetentionScheduleActionDefinition, String)} (RetentionSchedule, String, String)}
     */
    public RetentionScheduleActionDefinition createRetentionScheduleStep(RetentionScheduleActionDefinition retentionScheduleActionDefinition, String retentionScheduleId)
    {
        return createRetentionScheduleStep(retentionScheduleActionDefinition, retentionScheduleId, EMPTY);
    }

    /**
     * Gets the retentionSchedule of a record category.
     *
     * @param retentionScheduleId The identifier of a record category
     * @param parameters The URL parameters to add
     * @return The {@link RetentionScheduleActionDefinition} for the given {@code recordCategoryId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code recordCategoryId}</li>
     *  <li>{@code recordCategoryId} does not exist</li>
     *</ul>
     */
    public RetentionScheduleStepCollection getRetentionScheduleStep(String retentionScheduleId, String parameters)
    {
        mandatoryString("retentionScheduleId", retentionScheduleId);

        return getRmRestWrapper().processModels(RetentionScheduleStepCollection.class, simpleRequest(
            GET,
            "retention-schedules/{retentionScheduleId}/retention-steps?{parameters}",
            retentionScheduleId,
            parameters
        ));
    }

    /**
     * See {@link #getRetentionScheduleStep(String, String)}
     */
    public RetentionScheduleStepCollection getRetentionScheduleStep(String recordCategoryId)
    {
        return getRetentionScheduleStep(recordCategoryId, EMPTY);
    }
}

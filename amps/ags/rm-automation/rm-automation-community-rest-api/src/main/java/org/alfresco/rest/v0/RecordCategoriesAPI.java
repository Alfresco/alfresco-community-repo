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
package org.alfresco.rest.v0;

import static org.apache.http.HttpStatus.SC_OK;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API on record categories
 *
 * @author Oana Nechiforescu
 * @since 2.5
 */
@Component
public class RecordCategoriesAPI extends BaseAPI
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordCategoriesAPI.class);
    private static final String RM_ACTIONS_API = "{0}rma/actions/ExecutionQueue";
    private static final String DISPOSITION_ACTIONS_API = "{0}node/{1}/dispositionschedule/dispositionactiondefinitions";
    private static final String DISPOSITION_SCHEDULE_API = "{0}node/{1}/dispositionschedule";


    /**
     * Creates a retention schedule for the category given as parameter
     *
     * @param user         the user creating the disposition schedule
     * @param password     the user's password
     * @param categoryName the category name to create the retention schedule for
     * @return The HTTP Response.
     */
    public HttpResponse createRetentionSchedule(String user, String password, String categoryName)
    {
        String catNodeRef = getNodeRefSpacesStore() + getItemNodeRef(user, password, "/" + categoryName);

        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "createDispositionSchedule");
        requestParams.put("nodeRef", catNodeRef);

        return doPostJsonRequest(user, password, SC_OK, requestParams, RM_ACTIONS_API);
    }

    /**
     * Get the disposition schedule nodeRef
     *
     * @param user
     * @param password
     * @param categoryName
     * @return the disposition schedule nodeRef
     */
    public String getDispositionScheduleNodeRef(String user, String password, String categoryName)
    {
        String catNodeRef = NODE_PREFIX + getItemNodeRef(user, password, "/" + categoryName);
        JSONObject dispositionSchedule = doGetRequest(user, password, MessageFormat.format(DISPOSITION_SCHEDULE_API, "{0}", catNodeRef));
        return dispositionSchedule.getJSONObject("data").getString("nodeRef").replace(getNodeRefSpacesStore(), "");
    }

    /**
     * Sets retention schedule authority and instructions, also if it is applied to records or folders
     *
     * @param user             the user creating the disposition schedule
     * @param password         the user's password
     * @param retentionNodeRef the retention nodeRef
     * @return The HTTP Response.
     */
    public HttpResponse setRetentionScheduleGeneralFields(String user, String password, String retentionNodeRef, Map<RETENTION_SCHEDULE, String> retentionProperties, Boolean appliedToRecords)
    {
        String dispRetentionNodeRef = NODE_PREFIX + retentionNodeRef;

        JSONObject requestParams = new JSONObject();
        requestParams.put("prop_rma_dispositionAuthority", getPropertyValue(retentionProperties, RETENTION_SCHEDULE.RETENTION_AUTHORITY));
        requestParams.put("prop_rma_dispositionInstructions", getPropertyValue(retentionProperties, RETENTION_SCHEDULE.RETENTION_INSTRUCTIONS));
        requestParams.put("prop_rma_recordLevelDisposition", appliedToRecords.toString());
        return doPostJsonRequest(user, password, SC_OK, requestParams, MessageFormat.format(UPDATE_METADATA_API, "{0}", dispRetentionNodeRef));
    }

    /**
     * Creates a retention schedule steps for the category given as parameter
     *
     * @param user         the user creating the disposition schedule
     * @param password     the user's password
     * @param categoryName the category name to create the retention schedule for
     * @return The HTTP Response.
     */
    public HttpResponse addDispositionScheduleSteps(String user, String password, String categoryName, Map<RETENTION_SCHEDULE, String> properties)
    {
        String catNodeRef = NODE_PREFIX + getItemNodeRef(user, password, "/" + categoryName);

        JSONObject requestParams = new JSONObject();
        addPropertyToRequest(requestParams, "name", properties, RETENTION_SCHEDULE.NAME);
        addPropertyToRequest(requestParams, "description", properties, RETENTION_SCHEDULE.DESCRIPTION);
        addPropertyToRequest(requestParams, "period", properties, RETENTION_SCHEDULE.RETENTION_PERIOD);
        addPropertyToRequest(requestParams, "ghostOnDestroy", properties, RETENTION_SCHEDULE.RETENTION_GHOST);
        addPropertyToRequest(requestParams, "periodProperty", properties, RETENTION_SCHEDULE.RETENTION_PERIOD_PROPERTY);
        addPropertyToRequest(requestParams, "location", properties, RETENTION_SCHEDULE.RETENTION_LOCATION);
        String events = getPropertyValue(properties, RETENTION_SCHEDULE.RETENTION_EVENTS);
        if(!events.equals(""))
        {
            requestParams.append("events", events);
        }
        addPropertyToRequest(requestParams, "combineDispositionStepConditions", properties, RETENTION_SCHEDULE.COMBINE_DISPOSITION_STEP_CONDITIONS);
        addPropertyToRequest(requestParams, "eligibleOnFirstCompleteEvent", properties, RETENTION_SCHEDULE.RETENTION_ELIGIBLE_FIRST_EVENT);

        return doPostJsonRequest(user, password, SC_OK, requestParams, MessageFormat.format(DISPOSITION_ACTIONS_API, "{0}", catNodeRef));
    }

    /**
     * Delete a category
     *
     * @param username     user's username
     * @param password     its password
     * @param categoryName the name of the category
     * @throws AssertionError if the delete was unsuccessful.
     */
    public void deleteCategory(String username, String password, String categoryName)
    {
        deleteItem(username, password, "/" + categoryName);
    }

    /**
     * Delete a sub-category
     *
     * @param username     user's username
     * @param password     its password
     * @param categoryName the name of the sub-category
     * @throws AssertionError if the deletion was unsuccessful.
     */
    public void deleteSubCategory(String username, String password, String categoryName, String subCategoryName)
    {
        deleteItem(username, password, "/" + categoryName + "/" + subCategoryName);
    }

    /**
     * Delete a folder inside a container in RM site
     *
     * @param username      user's username
     * @param password      its password
     * @param folderName    folder name
     * @param containerName the name of the category or container sin which the folder is
     * @throws AssertionError if the deletion was unsuccessful.
     */
    public void deleteFolderInContainer(String username, String password, String folderName, String containerName)
    {
        deleteItem(username, password, "/" + containerName + "/" + folderName);
    }

    /**
     * Returns a map of retention properties
     *
     * @param authority    retention authority
     * @param instructions retention authority
     * @return the map
     */
    public Map<RETENTION_SCHEDULE, String> getRetentionProperties(String authority, String instructions)
    {
        Map<RETENTION_SCHEDULE, String> retentionProperties = new HashMap<>();
        retentionProperties.put(RETENTION_SCHEDULE.RETENTION_AUTHORITY, authority);
        retentionProperties.put(RETENTION_SCHEDULE.RETENTION_INSTRUCTIONS, instructions);
        return retentionProperties;
    }
}

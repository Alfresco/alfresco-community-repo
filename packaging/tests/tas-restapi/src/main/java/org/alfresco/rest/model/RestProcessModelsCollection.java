/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.alfresco.rest.core.RestModels;

/**
 * Handles collection of Processes
 * Example
 * {
 *      "list": {
 *      "pagination": {
 *          "count": 100,
 *          "hasMoreItems": true,
 *          "totalItems": 849,
 *          "skipCount": 0,
 *          "maxItems": 100
 *       ,
 * "entries": [
 * {
 *     "entry": {
 *         "processDefinitionId": "activitiAdhoc:1:4",
 *          "startUserId": "admin",
 *          "startActivityId": "start",
 *          "startedAt": "2016-05-24T09:43:17.000+0000",
 *          "id": "55069",
 *          "completed": false,
 *          "processDefinitionKey": "activitiAdhoc"
 *     }
 * },
 * ]
 * Created by Claudia Agache on 10/11/2016.
 */
public class RestProcessModelsCollection extends RestModels<RestProcessModel, RestProcessModelsCollection>
{
    public RestProcessModel getProcessModelByProcessDefId(String processDefinitionId)
    {
        List<RestProcessModel> processesList = getEntries();
        for (RestProcessModel processModel : processesList)
        {
            if (processModel.onModel().getId().equals(processDefinitionId))
            {
                STEP(String.format("REST API: Get process with process definition '%s'", processDefinitionId));
                return processModel.onModel();
            }
        }
        return null;
    }
}

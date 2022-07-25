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
 * Handle collection of <RestProcessDefinitionModel>
 * "entries": [
 * {
 * "entry": {
 * "deploymentId": "1",
 * "name": "Adhoc Activiti Process",
 * "description": "Assign a new task to yourself or a colleague",
 * "id": "activitiAdhoc:1:4",
 * "startFormResourceKey": "wf:submitAdhocTask",
 * "category": "http://alfresco.org",
 * "title": "New Task",
 * "version": 1,
 * "graphicNotationDefined": true,
 * "key": "activitiAdhoc"
 * }
 * },
 * ]
 * Created by Claudia Agache on 10/13/2016.
 */
public class RestProcessDefinitionModelsCollection extends RestModels<RestProcessDefinitionModel, RestProcessDefinitionModelsCollection>
{
    public RestProcessDefinitionModel getProcessDefinitionByDeploymentId(String deploymentId)
    {
        STEP(String.format("REST API: Get process definition with deploymentId '%s'", deploymentId));
        List<RestProcessDefinitionModel> processDefinitionsList = getEntries();

        for (RestProcessDefinitionModel processDefinitionEntry: processDefinitionsList)
        {
            if (processDefinitionEntry.onModel().getDeploymentId().equals(deploymentId))
            {
                return processDefinitionEntry.onModel();
            }
        }
        return null;
    }

    public RestProcessDefinitionModel getProcessDefinitionById(String id)
    {
        STEP(String.format("REST API: Get process definition with id '%s'", id));
        List<RestProcessDefinitionModel> processDefinitionsList = getEntries();

        for (RestProcessDefinitionModel processDefinitionEntry: processDefinitionsList)
        {
            if (processDefinitionEntry.onModel().getId().equals(id))
            {
                return processDefinitionEntry.onModel();
            }
        }
        return null;
    }

    public RestProcessDefinitionModel getProcessDefinitionByKey(String key)
    {
        STEP(String.format("REST API: Get process definition with key '%s'", key));
        List<RestProcessDefinitionModel> processDefinitionsList = getEntries();

        for (RestProcessDefinitionModel processDefinitionEntry: processDefinitionsList)
        {
            if (processDefinitionEntry.onModel().getKey().equals(key))
            {
                return processDefinitionEntry.onModel();
            }
        }
        return null;
    }
}    

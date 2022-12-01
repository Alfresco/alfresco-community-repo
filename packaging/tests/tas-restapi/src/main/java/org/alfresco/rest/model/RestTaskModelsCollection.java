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
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

/**
 * Handle collection of <RestTaskModel>
 * 
 * @author Critina Axinte
 */
public class RestTaskModelsCollection extends RestModels<RestTaskModel, RestTaskModelsCollection>
{    
    public RestTaskModel getTaskModelByAssignee(UserModel assigneeName)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getAssignee().equals(assigneeName.getUsername()))
            {
                STEP(String.format("REST API: Get task with assignee '%s'", assigneeName.getUsername()));
                return taskModel.onModel();
            }
        }

        return null;
    }
    
    public RestTaskModel getTaskModelByDescription(SiteModel siteModel)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getDescription().equals(String.format("Request to join %s site", siteModel.getId())))
            {
                STEP(String.format("REST API: Get task with site name '%s'", siteModel.getId()));
                return taskModel.onModel();
            }
        }

        return null;
    }
    
    public RestTaskModel getTaskModelByProcess(ProcessModel process)
    {
        List<RestTaskModel> tasksList = getEntries();

        for (RestTaskModel taskModel: tasksList)
        {
            if (taskModel.onModel().getProcessId().equals(process.getId()))
            {
                STEP(String.format("REST API: Get task with processId '%s'", process.getId()));
                return taskModel.onModel();
            }
        }

        return null;
    }
}    

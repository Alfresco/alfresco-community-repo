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
 * "entries": [
 * {
 *      "entry": {
 *          "name": "parallel-review-group.bpmn20.xml",
 *          "id": "17",
 *          "deployedAt": "2016-03-04T11:28:02.000+0000"
 *      }
 * },
 * {
 *      "entry": {
 *          "name": "review.bpmn20.xml",
 *          "id": "5",
 *          "deployedAt": "2016-03-04T11:28:01.000+0000"
 *      }
 * }
 * ]
 * Created by Claudia Agache on 10/4/2016.
 */
public class RestDeploymentModelsCollection extends RestModels<RestDeploymentModel, RestDeploymentModelsCollection>
{    

    public RestDeploymentModel getDeploymentByName(String deploymentName)
    {
        STEP(String.format("REST API: Get deployment with name '%s'", deploymentName));
        List<RestDeploymentModel> deploymentsList = getEntries();

        for (RestDeploymentModel deploymentEntry: deploymentsList)
        {
            if (deploymentEntry.onModel().getName().equals(deploymentName))
            {
                return deploymentEntry.onModel();
            }
        }

        return null;
    }
}    

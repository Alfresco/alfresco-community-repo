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
 * Handle collection of <RestFormModel>
 * "entries": [
 * "entry": {
 * "qualifiedName": "{http://www.alfresco.org/model/bpm/1.0}reassignable",
 * "defaultValue": "true",
 * "dataType": "d:boolean",
 * "name": "bpm_reassignable",
 * "required": false
 * }
 * },
 * {
 * "entry": {
 * "allowedValues": [
 * "1",
 * "2",
 * "3"
 * ],
 * "qualifiedName": "{http://www.alfresco.org/model/bpm/1.0}workflowPriority",
 * "defaultValue": "2",
 * "dataType": "d:int",
 * "name": "bpm_workflowPriority",
 * "title": "Workflow Priority",
 * "required": false
 * }
 * },
 * ]
 * Created by Claudia Agache on 10/18/2016.
 */
public class RestFormModelsCollection extends RestModels<RestFormModel, RestFormModelsCollection>
{
    public RestFormModel getStartFormModelByQualifiedName(String qualifiedName)
    {
        STEP(String.format("REST API: Get start form model by qualified name '%s'", qualifiedName));
        List<RestFormModel> startFormModels = getEntries();

        for (RestFormModel startFormModel: startFormModels)
        {
            if (startFormModel.onModel().getQualifiedName().equals(qualifiedName))
            {
                return startFormModel.onModel();
            }
        }
        return null;
    }
}    

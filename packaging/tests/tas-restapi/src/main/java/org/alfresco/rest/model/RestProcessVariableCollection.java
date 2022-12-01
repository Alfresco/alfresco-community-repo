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

import org.alfresco.rest.core.RestModels;

import java.util.List;

import static org.alfresco.utility.report.log.Step.STEP;

/**
 * 
 "entries": [
      {"entry": {
         "name": "bpm_package",
         "type": "bpm:workflowPackage",
         "value": "workspace://SpacesStore/ab728441-84f4-4d61-bb04-c51822b114fe"
      }},
 *
 */
public class RestProcessVariableCollection extends RestModels<RestProcessVariableModel, RestProcessVariableCollection>
{
    public RestProcessVariableModel getProcessVariableByName(String name)
    {
        STEP(String.format("REST API: Get process variable entry with name '%s'", name));
        List<RestProcessVariableModel> processVariablesList = getEntries();

        for (RestProcessVariableModel variable: processVariablesList)
        {
            if (variable.onModel().getName().equals(name))
            {
                return variable.onModel();
            }
        }
        return null;
    }
}    

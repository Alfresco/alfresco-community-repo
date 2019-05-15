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
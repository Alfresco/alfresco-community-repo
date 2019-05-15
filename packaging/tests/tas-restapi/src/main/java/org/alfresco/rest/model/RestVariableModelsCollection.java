package org.alfresco.rest.model;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.List;

import org.alfresco.rest.core.RestModels;

/**
 * Handles a collection of <RestVariableModel> JSON response
 * Example:
 * "variables": [
            {
              "scope": "string",
              "name": "string",
              "value": 0,
              "type": "string"
            }
          ]
          
 * @author Cristina Axinte
 *
 */
public class RestVariableModelsCollection extends RestModels<RestVariableModel, RestVariableModelsCollection>
{     
    public RestVariableModel getVariableByName(String variableName)
    {
        STEP(String.format("REST API: Get variable with name '%s'", variableName));
        List<RestVariableModel> variableList = getEntries();

        for (RestVariableModel variableEntry: variableList)
        {
            if (variableEntry.onModel().getName().equals(variableName))
            {
                return variableEntry.onModel();
            }
        }

        return null;
    }
}    
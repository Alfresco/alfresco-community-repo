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
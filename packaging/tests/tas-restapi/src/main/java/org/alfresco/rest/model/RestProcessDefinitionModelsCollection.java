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
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

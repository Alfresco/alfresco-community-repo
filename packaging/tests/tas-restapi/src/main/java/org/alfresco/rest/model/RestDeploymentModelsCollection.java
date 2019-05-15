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
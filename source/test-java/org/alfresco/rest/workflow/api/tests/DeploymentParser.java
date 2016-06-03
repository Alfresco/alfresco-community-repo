package org.alfresco.rest.workflow.api.tests;

import org.alfresco.rest.workflow.api.model.Deployment;
import org.json.simple.JSONObject;

public class DeploymentParser extends ListParser<Deployment>
{
    public static DeploymentParser INSTANCE = new DeploymentParser();

    @Override
    public Deployment parseEntry(JSONObject entry)
    {
        Deployment deployment = new Deployment();
        deployment.setId((String) entry.get("id"));
        deployment.setName((String) entry.get("name"));
        deployment.setDeployedAt(WorkflowApiClient.parseDate(entry, "deployedAt"));
        return deployment;
    }
}

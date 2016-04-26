package org.alfresco.rest.workflow.api.tests;

import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.json.simple.JSONObject;

public class ProcessDefinitionParser extends ListParser<ProcessDefinition>
{
    public static ProcessDefinitionParser INSTANCE = new ProcessDefinitionParser();

    @Override
    public ProcessDefinition parseEntry(JSONObject entry)
    {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId((String) entry.get("id"));
        processDefinition.setKey((String) entry.get("key"));
        processDefinition.setVersion(((Number) entry.get("version")).intValue());
        processDefinition.setName((String) entry.get("name"));
        processDefinition.setDeploymentId((String) entry.get("deploymentId"));
        processDefinition.setTitle((String) entry.get("title"));
        processDefinition.setDescription((String) entry.get("description"));
        processDefinition.setCategory((String) entry.get("category"));
        processDefinition.setStartFormResourceKey((String) entry.get("startFormResourceKey"));
        processDefinition.setGraphicNotationDefined((Boolean) entry.get("graphicNotationDefined"));
        return processDefinition;
    }
}

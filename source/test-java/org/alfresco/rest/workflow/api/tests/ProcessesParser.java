/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.workflow.api.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.model.Variable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ProcessesParser extends ListParser<ProcessInfo>
{
    public static ProcessesParser INSTANCE = new ProcessesParser();

    @SuppressWarnings("unchecked")
    @Override
    public ProcessInfo parseEntry(JSONObject entry)
    {
        ProcessInfo processesRest = new ProcessInfo();
        processesRest.setId((String) entry.get("id"));
        processesRest.setProcessDefinitionId((String) entry.get("processDefinitionId"));
        processesRest.setProcessDefinitionKey((String) entry.get("processDefinitionKey"));
        processesRest.setStartedAt(WorkflowApiClient.parseDate(entry, "startedAt"));
        processesRest.setEndedAt(WorkflowApiClient.parseDate(entry, "endedAt"));
        processesRest.setDurationInMs((Long) entry.get("durationInMs"));
        processesRest.setDeleteReason((String) entry.get("deleteReason"));
        processesRest.setBusinessKey((String) entry.get("businessKey"));
        processesRest.setSuperProcessInstanceId((String) entry.get("superProcessInstanceId"));
        processesRest.setStartActivityId((String) entry.get("startActivityId"));
        processesRest.setStartUserId((String) entry.get("startUserId"));
        processesRest.setEndActivityId((String) entry.get("endActivityId"));
        processesRest.setCompleted((Boolean) entry.get("completed"));
        processesRest.setVariables((Map<String,Object>) entry.get("variables"));
        processesRest.setItems((Set<String>) entry.get("item"));
        
        if (entry.get("processVariables") != null) {
            List<Variable> processVariables = new ArrayList<Variable>();
            JSONArray variables = (JSONArray) entry.get("processVariables");
            for (int i = 0; i < variables.size(); i++) 
            {
                JSONObject variableJSON = (JSONObject) variables.get(i);
                Variable variable = new Variable();
                variable.setName((String) variableJSON.get("name"));
                variable.setType((String) variableJSON.get("type"));
                variable.setValue(variableJSON.get("value"));
                processVariables.add(variable);
            }
            processesRest.setProcessVariables(processVariables);
        }
        
        return processesRest;
    }
}

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
package org.alfresco.rest.requests.workflowAPI;

import javax.json.JsonObject;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestDeploymentModel;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.rest.requests.Deployments;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.requests.ProcessDefinitions;
import org.alfresco.rest.requests.Processes;
import org.alfresco.rest.requests.Task;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.UserModel;

import io.restassured.RestAssured;

/**
 * Defines the entire Rest Workflow API
 * {@link https://api-explorer.alfresco.com/api-explorer/} select "Workflow API"
 */
public class RestWorkflowAPI extends ModelRequest<RestWorkflowAPI>
{
    public RestWorkflowAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/workflow/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Provides DSL on all REST calls under <code>/processes/{processId}/...</code> API path
     * 
     * @return {@link RestProcessModel}
     */
    public RestProcessModel addProcess(String processDefinitionKey, UserModel assignee, boolean sendEmailNotifications, Priority priority)
    {
        return new Processes(restWrapper).addProcess(processDefinitionKey, assignee, sendEmailNotifications, priority);
    }
    
    /**
     * Provides DSL on all REST calls under <code>/processes/{processId}/...</code> API path
     * 
     * @return {@link RestProcessModel}
     */
    public RestProcessModel addProcessWithBody(JsonObject postBody)
    {
        return new Processes(restWrapper).addProcessWithBody(postBody.toString());
    }

    /**
     * Provides DSL on all REST calls under <code>/tasks/{taskId}/...</code> API path
     * 
     * @return {@link Task}
     */
    public Task usingTask(TaskModel task)
    {
        return new Task(restWrapper, task);
    }

    /**
     * Provides get all process <code>/processes</code>of current user logged in
     * 
     * @return {@link RestProcessModelsCollection}
     */
    public RestProcessModelsCollection getProcesses()
    {
        return new Processes(restWrapper).getProcesses();
    }

    /**
     * Provides all deployments of current user logged in
     * 
     * @return {@link RestDeploymentModelsCollection}
     */
    public RestDeploymentModelsCollection getDeployments()
    {
        return new Deployments(restWrapper).getDeployments();
    }

    /**
     * Provides DSL on all REST calls under <code>/deployments/{deploymentID}</code> API path
     * 
     * @return {@link Deployments}
     */
    public Deployments usingDeployment(RestDeploymentModel deployment)
    {
        return new Deployments(deployment, restWrapper);
    }

    /**
     * Provides all ProcessDefinitions of current user logged in
     * 
     * @return {@link RestProcessDefinitionModelsCollection}
     */
    public RestProcessDefinitionModelsCollection getAllProcessDefinitions()
    {
        return new ProcessDefinitions(restWrapper).getAllProcessDefinitions();
    }

    /**
     * Provides DSL on all REST calls under <code>/process-definition/{processDefinitionID}<code> API path
     * 
     * @return {@link ProcessDefinitions}
     */
    public ProcessDefinitions usingProcessDefinitions(RestProcessDefinitionModel processDefinition)
    {
        return new ProcessDefinitions(processDefinition, restWrapper);
    }

    /**
     * Provides DSL on all REST calls under <code>/process-definition/{processDefinitionID}</code> API path
     * 
     * @return {@link Processes}
     */
    public Processes usingProcess(ProcessModel processModel)
    {
        return new Processes(processModel, restWrapper);
    }

    /**
     * Provides all tasks of the current user
     * 
     * @return {@link RestTaskModelsCollection}
     */
    public RestTaskModelsCollection getTasks()
    {
        return new Task(restWrapper, null).getTasks();
    }
}

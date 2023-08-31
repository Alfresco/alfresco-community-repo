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
package org.alfresco.rest.requests;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestCandidateModelsCollection;
import org.alfresco.rest.model.RestFormModelsCollection;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.rest.model.RestVariableModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TaskModel;
import org.springframework.http.HttpMethod;

public class Task extends ModelRequest<Task>
{
    private TaskModel task;
    
    public Task(RestWrapper restWrapper)
    {
        super(restWrapper);
    }
    
    public Task(RestWrapper restWrapper, TaskModel task)
    {
        super(restWrapper);
        this.task = task;
    }

    /**
     * Retrieve a list of tasks visible for the authenticated user using GET call on "/tasks"
     * 
     * @return
     */
    public RestTaskModelsCollection getTasks()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestTaskModelsCollection.class, request);
    }

    /**
     * Retrieve the tasks identified by taskId using GET call on "/tasks/{taskId}"
     * 
     * @param taskId
     * @return
     */
    public RestTaskModel getTask()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestTaskModel.class, request);
    }
    
    /**
     * Update task using PUT put /tasks/{taskId} call
     *
     * @param newStateValue
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTaskModel updateTask(String newStateValue)
    {
       return updateTask(JsonBodyGenerator.defineJSON().add("state", newStateValue).build());
    }
       
    /**
     * Update task using PUT put /tasks/{taskId} cal
     * 
     * @param inputJson the json used as input for PUT call
     * @return
     */
    public RestTaskModel updateTask(JsonObject inputJson)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, inputJson.toString(), "tasks/{taskId}?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestTaskModel.class, request);
    }
    
    /**
     * Retrieve the task variables using GET call on "/tasks/{taskId}/variables"
     * 
     * @param taskId
     * @return
     */
    public RestVariableModelsCollection getTaskVariables()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}/variables?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestVariableModelsCollection.class, request);
    }
    
    /**
     * Update/Add task variable using PUT put /tasks/{taskId}/variables/{variableName} call
     *
     * @param taskId
     * @param variableName
     * @return
     * @throws JsonToModelConversionException
     */
    public RestVariableModel updateTaskVariable(RestVariableModel variableModel)
    {
        String postBody = JsonBodyGenerator.taskVariable(variableModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}/variables/{variableName}", task.getId(),
                variableModel.getName());
        return restWrapper.processModel(RestVariableModel.class, request);
    }
    
    /**
     * Add task variable using POST /tasks/{taskId}/variables call
     *
     * @param taskId
     * @return
     * @throws JsonToModelConversionException
     */
    public RestVariableModel addTaskVariable(RestVariableModel variableModel)
    {
        String postBody = JsonBodyGenerator.taskVariable(variableModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "tasks/{taskId}/variables", task.getId());
        return restWrapper.processModel(RestVariableModel.class, request);
    }
    
    /**
     * Add task variables using POST /tasks/{taskId}/variables call
     *
     * @param taskId
     * @return
     * @throws JsonToModelConversionException
     */
    public RestVariableModelsCollection addTaskVariables(RestVariableModel... taskVariablesModel)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();        
        for(RestVariableModel taskVariableModel: taskVariablesModel)
        {      
            array.add(JsonBodyGenerator.defineJSON().add("scope", taskVariableModel.getScope()).add("name", taskVariableModel.getName())
                    .add("type", taskVariableModel.getType()).add("value", taskVariableModel.getValue().toString()));
        }      
       
        String postBody = array.build().toString();  
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "tasks/{taskId}/variables", task.getId());
        return restWrapper.processModels(RestVariableModelsCollection.class, request);
    }
    
    /**
     * Delete task variable using DELETE /tasks/{taskId}/variables/{variableName} call
     *
     * @param taskId
     * @param variableName
     * @return
     * @throws JsonToModelConversionException
     */
    public void deleteTaskVariable(RestVariableModel variableModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "tasks/{taskId}/variables/{variableName} ", task.getId(),
                variableModel.getName());
        restWrapper.processEmptyModel(request);            
    }

    /**
     * Add task item using POST /tasks/{taskId}/items
     *
     * @param processId
     * @return
     * @throws JsonToModelConversionException
     */
    public RestItemModel addTaskItem(FileModel fileModel)
    {
        String postBody = JsonBodyGenerator.keyValueJson("id", fileModel.getNodeRef().split(";")[0]);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "tasks/{taskId}/items", task.getId());
        return restWrapper.processModel(RestItemModel.class, request);
    }
    
    /**
     * Add task items using POST /tasks/{taskId}/items
     *
     * @param fileModels
     * @return
     * @throws JsonToModelConversionException
     */
    public RestItemModelsCollection addTaskItems(FileModel... fileModels)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();        
        for(FileModel fileModel: fileModels)
        {                    
            array.add(JsonBodyGenerator.defineJSON().add("id", fileModel.getNodeRef().split(";")[0]));
        }      
       
        String postBody = array.build().toString();      
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "tasks/{taskId}/items", task.getId());
        return restWrapper.processModels(RestItemModelsCollection.class, request);
    }
    
    
    /**
     * Retrieve the task items using GET call on "/tasks/{taskId}/items"
     * 
     * @param taskId
     * @return
     */
    public RestItemModelsCollection getTaskItems()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}/items?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestItemModelsCollection.class, request);
    }

    /**
     * Retrieves models of the task form type definition
     * @param taskModel
     * @return
     */
    public RestFormModelsCollection getTaskFormModel()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}/task-form-model?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestFormModelsCollection.class, request);
    }
    
    /**
     * Delete a task item using DELETE call on tasks/{taskId}/items/{itemId}
     *
     * @param taskId
     * @param itemId
     */
    public void deleteTaskItem(RestItemModel itemModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "tasks/{taskId}/items/{itemId}", task.getId(),
                itemModel.getId());
        restWrapper.processEmptyModel(request);
    }
    
    /**
     * Retrieve the task candidates (users and groups) using GET call on "/tasks/{taskId}/candidates"
     * 
     * @param taskId
     * @return
     */
    public RestCandidateModelsCollection getTaskCandidates()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}/candidates?{parameters}", task.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestCandidateModelsCollection.class, request);
    }
}

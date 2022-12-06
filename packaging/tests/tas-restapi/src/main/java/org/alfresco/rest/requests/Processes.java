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

import javax.json.JsonArrayBuilder;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpMethod;

/**
 * Created by Claudia Agache on 10/11/2016.
 */

public class Processes extends ModelRequest<Processes>  
{
    private ProcessModel processModel;
    public Processes(RestWrapper restWrapper) 
    {
        super(restWrapper);
    }

    public Processes(ProcessModel processModel, RestWrapper restWrapper) {
        this(restWrapper);
        this.processModel = processModel;
    }

    /**
     * Retrieve 100 processes (this is the default size when maxItems is not specified) from Alfresco using GET call on "/processes"
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessModelsCollection getProcesses()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "processes?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestProcessModelsCollection.class, request);
    }

    /**
     * Retrieve all process variables from Alfresco using GET /processes/{processId}/variables
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessVariableCollection getProcessVariables()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "processes/{processId}/variables?{parameters}", processModel.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestProcessVariableCollection.class, request);
    }

    /**
     * Retrieves the process identified by processId using GET /processes/{processId}
     * @return
     */
    public RestProcessModel getProcess()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "processes/{processId}?{parameters}", processModel.getId(), restWrapper.getParameters());
        return restWrapper.processModel(RestProcessModel.class, request);
    }

    /**
     * Delete a process using DELETE call on processes/{processId}
     */
    public void deleteProcess()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "processes/{processId}", processModel.getId());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Starts new process using POST /processes
     *
     * @param processDefinitionKey
     * @param assignee
     * @param sendEmailNotifications
     * @param priority
     * @return
     */
    public RestProcessModel addProcess(String processDefinitionKey, UserModel assignee, boolean sendEmailNotifications, Priority priority)
    {
        String postBody = JsonBodyGenerator.process(processDefinitionKey, assignee, sendEmailNotifications, priority);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes");
        return restWrapper.processModel(RestProcessModel.class, request);
    }
    
    /**
     * Starts new process with given input body using POST /processes
     *
     * @param postBody
     * @return
     */
    public RestProcessModel addProcessWithBody(String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes");
        return restWrapper.processModel(RestProcessModel.class, request);
    }

    /**
     * Add process variable using POST /processes/{processId}/variables
     *
     * @param variableModel
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessVariableModel addProcessVariable(RestProcessVariableModel variableModel)
    {
        String postBody = JsonBodyGenerator.processVariable(variableModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes/{processId}/variables", processModel.getId());
        return restWrapper.processModel(RestProcessVariableModel.class, request);
    }
    
    /**
     * Add process variables using POST /processes/{processId}/variables
     *
     * @param processVariablesModel
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessVariableCollection addProcessVariables(RestProcessVariableModel... processVariablesModel)
    {      
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();        
        for(RestProcessVariableModel processVariableModel: processVariablesModel)
        {       
            array.add(JsonBodyGenerator.defineJSON().add("name", processVariableModel.getName())
                    .add("value", processVariableModel.getValue())
                    .add("type", processVariableModel.getType())).toString();
        }      
       
        String postBody = array.build().toString();    
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes/{processId}/variables", processModel.getId());
        return restWrapper.processModels(RestProcessVariableCollection.class, request);
    }


    /**
     * Retrieve all process items from Alfresco using GET /processes/{processId}/items
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestItemModelsCollection getProcessItems()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "processes/{processId}/items?{parameters}", processModel.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestItemModelsCollection.class, request);
    }

    /**
     * Delete a process variable using DELETE call on processes/{processId}/variables/{variableName}
     *
     * @param variableModel
     */
    public void deleteProcessVariable(RestProcessVariableModel variableModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "processes/{processId}/variables/{variableName}", processModel.getId(),
                variableModel.getName());
        restWrapper.processEmptyModel(request);
    }

    /**
     * Update/Add process variable using PUT /processes/{processId}/variables/{variableName}
     *
     * @param variableModel
     * @return
     * @throws JsonToModelConversionException
     */
    public RestProcessVariableModel updateProcessVariable(RestProcessVariableModel variableModel)
    {
        String postBody = JsonBodyGenerator.processVariable(variableModel);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "processes/{processId}/variables/{variableName}", processModel.getId(),
                variableModel.getName());
        return restWrapper.processModel(RestProcessVariableModel.class, request);
    }
        
    /**
     * Retrieve all tasks of a specified process from Alfresco using GET /processes/{processId}/tasks
     *
     * @return
     * @throws JsonToModelConversionException
     */
    public RestTaskModelsCollection getProcessTasks()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "processes/{processId}/tasks?{parameters}", processModel.getId(), restWrapper.getParameters());
        return restWrapper.processModels(RestTaskModelsCollection.class, request);
    }

    /**
     * Add process item using POST /processes/{processId}/items
     *
     * @param fileModel
     * @return
     * @throws JsonToModelConversionException
     */
    public RestItemModel addProcessItem(FileModel fileModel)
    {
        String postBody = JsonBodyGenerator.keyValueJson("id", fileModel.getNodeRefWithoutVersion());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes/{processId}/items", processModel.getId());
        return restWrapper.processModel(RestItemModel.class, request);
    }
    
    /**
     * Add process items using POST /processes/{processId}/items
     *
     * @param fileModels
     * @return
     * @throws JsonToModelConversionException
     */
    public RestItemModelsCollection addProcessItems(FileModel... fileModels)
    {
        JsonArrayBuilder array = JsonBodyGenerator.defineJSONArray();        
        for(FileModel fileModel: fileModels)
        {                    
            array.add(JsonBodyGenerator.defineJSON().add("id", fileModel.getNodeRefWithoutVersion()));
        }      
       
        String postBody = array.build().toString();        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "processes/{processId}/items", processModel.getId());
        return restWrapper.processModels(RestItemModelsCollection.class, request);
    }

    /**
     * Delete a process item using DELETE call on processes/{processId}/items/{itemId}
     *
     * @param itemModel
     */
    public void deleteProcessItem(RestItemModel itemModel)
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "processes/{processId}/items/{itemId}", processModel.getId(), itemModel.getId());
        restWrapper.processEmptyModel(request);
    }
}

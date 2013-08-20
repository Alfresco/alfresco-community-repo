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
package org.alfresco.rest.workflow.api.tasks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.model.Task;

@EntityResource(name="tasks", title = "Tasks")
public class TasksRestEntityResource implements EntityResourceAction.Read<Task>, 
                                                           EntityResourceAction.ReadById<Task>, EntityResourceAction.Update<Task> {

    Tasks tasks;
    
    public void setTasks(Tasks tasks)
    {
        this.tasks = tasks;
    }

    @Override
    @WebApiDescription(title = "Get Tasks", description = "Get information for tasks")
    @WebApiParameters(value = { 
            @WebApiParam(name = "status", title = "The status of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "assignee", title = "The assignee of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "owner", title = "The owner of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "candidateUser", title = "A candidate user of the task, only returns unassigned tasks. Can only be used toghether with status parameter set to 'active'", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "candidateGroup", title = "A candidate group of the task, only returns unassigned tasks. Can only be used toghether with status parameter set to 'active'", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "name", title = "The name of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "description", title = "The description of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "priority", title = "The priotiry of the task", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "processInstanceId", title = "The id of the process instance this task is part of", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "processInstanceBusinessKey", title = "The unique business key of the process instance this task is part of", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "startedAt", title = "The date the task was creates/started", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "dueAt", title = "The date the task is due", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "activityDefinitionId", title = "The id of the activity definition in the process", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "processDefinitionId", title = "The id of the process definition for the process this task is part of", kind=ResourceParameter.KIND.QUERY_STRING),
            @WebApiParam(name = "processDefinitionName", title = "The name of the process definition for the process this task is part of", kind=ResourceParameter.KIND.QUERY_STRING)})
    public CollectionWithPagingInfo<Task> readAll(Parameters params)
    {
        return tasks.getTasks(params);
    }

    @Override
    public Task readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        return tasks.getTask(id);
    }

    @Override
    public Task update(String id, Task entity, Parameters parameters)
    {
        return tasks.update(id, entity, parameters);
    }
}

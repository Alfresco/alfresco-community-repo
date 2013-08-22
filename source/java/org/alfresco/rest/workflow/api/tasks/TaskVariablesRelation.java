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

import java.util.List;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.impl.TaskVariablesWalkerCallback;
import org.alfresco.rest.workflow.api.model.TaskVariable;
import org.alfresco.rest.workflow.api.model.VariableScope;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "variables", entityResource = TasksRestEntityResource.class, title = "Variables for the current task")
public class TaskVariablesRelation implements RelationshipResourceAction.Read<TaskVariable>, RelationshipResourceAction.Create<TaskVariable>,
    RelationshipResourceAction.Update<TaskVariable>, RelationshipResourceAction.Delete
{
    private Tasks tasks;

	public void setTasks(Tasks tasks)
	{
		this.tasks = tasks;
	}

    /**
     * List the tasks variables.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Task Variables", description = "Get a paged list of the task variables")
    public CollectionWithPagingInfo<TaskVariable> readAll(String taskId, Parameters parameters)
    {
        VariableScope scope = VariableScope.ANY;
        if(parameters.getQuery() != null)
        {
            TaskVariablesWalkerCallback callback = new TaskVariablesWalkerCallback();
            QueryHelper.walk(parameters.getQuery(), callback);
            
            scope = callback.getScope();
        }
        return tasks.getTaskVariables(taskId, parameters.getPaging(), scope);
    }
    
    /**
     * Creates or updates multiple task variables. If the variable name doesn't exist yet it will be created
     */
    @Override
    @WebApiDescription(title = "Create or Update Variables", description = "Create or update multiple variable")
    public List<TaskVariable> create(String taskId, List<TaskVariable> variables, Parameters parameters)
    {
        return tasks.updateTaskVariables(taskId, variables);
    }

    /**
     * Update a task variable. If the variable name doesn't exist yet it will be created
     */
    @Override
    @WebApiDescription(title = "Update Task Variable", description = "Update a task variable")
    public TaskVariable update(String taskId, TaskVariable entity, Parameters parameters)
    {
        return tasks.updateTaskVariable(taskId, entity);
    }

    /**
     * Delete a task variable. If the variable name doesn't exist the delete call throws an exception.
     */
    @Override
    @WebApiDescription(title = "Delete Task Variable", description = "Delete a task variable")
    public void delete(String entityResourceId, String id, Parameters parameters)
    {
        tasks.deleteTaskVariable(entityResourceId, id);
    }

}

/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General  License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General  License for more details.
 *
 * You should have received a copy of the GNU Lesser General  License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.workflow.api;

import java.util.List;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.Item;
import org.alfresco.rest.workflow.api.model.Task;
import org.alfresco.rest.workflow.api.model.TaskCandidate;
import org.alfresco.rest.workflow.api.model.TaskVariable;
import org.alfresco.rest.workflow.api.model.Variable;
import org.alfresco.rest.workflow.api.model.VariableScope;

public interface Tasks
{
     CollectionWithPagingInfo<Task> getTasks(Parameters parameters);
     
     CollectionWithPagingInfo<Task> getTasks(String processId, Parameters parameters);
    
     Task getTask(String taskId);
    
     CollectionWithPagingInfo<TaskCandidate> getTaskCandidates(String taskId, Paging paging);
    
     Task update(String taskId, Task task, Parameters parameters);
    
     CollectionWithPagingInfo<FormModelElement> getTaskFormModel(String entityResourceId, Paging paging);
    
    // task variables
     CollectionWithPagingInfo<TaskVariable> getTaskVariables(String taskId, Paging paging, VariableScope scope);
    
     TaskVariable updateTaskVariable(String taskId, TaskVariable taskVariable);
     
     List<TaskVariable> updateTaskVariables(String taskId, List<TaskVariable> variables);
    
     void deleteTaskVariable(String taskId, String variableName);

     Item getItem(String taskId, String itemId);

     CollectionWithPagingInfo<Item> getItems(String taskId, Paging paging);

     Item createItem(String taskId, Item item);

     void deleteItem(String taskId, String itemId);
}

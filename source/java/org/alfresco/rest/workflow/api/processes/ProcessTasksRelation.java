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
package org.alfresco.rest.workflow.api.processes;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.model.Task;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "tasks", entityResource = ProcessesRestEntityResource.class, title = "Tasks for the current process")
public class ProcessTasksRelation implements RelationshipResourceAction.Read<Task>
{
    protected Tasks tasks;
    
    public void setTasks(Tasks tasks)
    {
        this.tasks = tasks;
    }

    /**
     * List the tasks.
     */
    @Override
    @WebApiDescription(title = "Get Tasks", description = "Get a paged list of the tasks")
    public CollectionWithPagingInfo<Task> readAll(String processId, Parameters parameters)
    {
        return tasks.getTasks(processId, parameters);
    }
}

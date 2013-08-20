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

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.model.FormModelElement;

/**
 * 
 * @author Frederik Heremans
 *
 */
@RelationshipResource(name = "task-form-model", entityResource = TasksRestEntityResource.class, title = "Model for the task's form")
public class TaskFormModelRelation implements RelationshipResourceAction.Read<FormModelElement>
{
    private Tasks tasks;

	public void setTasks(Tasks tasks)
	{
		this.tasks = tasks;
	}

    @Override
    public CollectionWithPagingInfo<FormModelElement> readAll(String entityResourceId,
                Parameters params)
    {
        return tasks.getTaskFormModel(entityResourceId, params.getPaging());
    }


}

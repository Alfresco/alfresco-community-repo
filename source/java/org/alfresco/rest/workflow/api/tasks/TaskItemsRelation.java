/* Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.model.Item;

/**
 * 
 * @author Frederik Heremans
 *
 */
@RelationshipResource(name = "items", entityResource = TasksRestEntityResource.class, title = "The items attached to a task")
public class TaskItemsRelation implements RelationshipResourceAction.ReadById<Item>, RelationshipResourceAction.Read<Item>, RelationshipResourceAction.Create<Item>, RelationshipResourceAction.Delete
{
    private Tasks tasks;

    public void setTasks(Tasks tasks)
    {
        this.tasks = tasks;
    }
    
	@Override
    public Item readById(String taskId, String itemId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        return tasks.getItem(taskId, itemId);
    }

    /**
     * List the attached items.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get the attached items of a Task", description = "Get a paged list of all the items")
    public CollectionWithPagingInfo<Item> readAll(String taskId, Parameters parameters)
    {
        return tasks.getItems(taskId, parameters.getPaging());
    }

    @Override
    public List<Item> create(String taskId, List<Item> entity, Parameters parameters)
    {
        List<Item> result = new ArrayList<Item>(entity.size());
        for (Item item : entity)
        {
           result.add(tasks.createItem(taskId, item));
        }
        return result;
    }
    
    @Override
    public void delete(String taskId, String itemId, Parameters parameters)
    {
       tasks.deleteItem(taskId, itemId);
    }
}

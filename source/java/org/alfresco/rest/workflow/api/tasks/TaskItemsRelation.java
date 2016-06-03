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

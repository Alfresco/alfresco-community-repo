package org.alfresco.rest.workflow.api.processes;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Processes;
import org.alfresco.rest.workflow.api.model.Item;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "items", entityResource = ProcessesRestEntityResource.class, title = "The items attached to a process instance")
public class ProcessItemsRelation implements RelationshipResourceAction.ReadById<Item>, RelationshipResourceAction.Read<Item>, RelationshipResourceAction.Create<Item>, RelationshipResourceAction.Delete
{
    private Processes processes;

	public void setProcesses(Processes processes)
	{
		this.processes = processes;
	}
	
	@Override
    public Item readById(String processId, String id, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        return processes.getItem(processId, id);
    }

    /**
     * List the attached items.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get the attached items of a Process Instance", description = "Get a paged list of all the possible items")
    public CollectionWithPagingInfo<Item> readAll(String processId, Parameters parameters)
    {
        return processes.getItems(processId, parameters.getPaging());
    }

    @Override
    public List<Item> create(String processId, List<Item> entity, Parameters parameters)
    {
        List<Item> result = new ArrayList<Item>(entity.size());
        for (Item item : entity)
        {
           result.add(processes.createItem(processId, item));
        }
        return result;
    }
    
    @Override
    public void delete(String processId, String id, Parameters parameters)
    {
        processes.deleteItem(processId, id);
        
    }
}

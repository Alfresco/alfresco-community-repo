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

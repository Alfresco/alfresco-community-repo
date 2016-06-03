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

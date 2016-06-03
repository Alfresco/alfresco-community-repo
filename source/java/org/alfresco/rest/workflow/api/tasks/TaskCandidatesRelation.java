package org.alfresco.rest.workflow.api.tasks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.model.TaskCandidate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Tijs Rademakers
 *
 */
@RelationshipResource(name = "candidates", entityResource = TasksRestEntityResource.class, title = "Candidates for the current task")
public class TaskCandidatesRelation implements RelationshipResourceAction.Read<TaskCandidate>
{
    private static final Log logger = LogFactory.getLog(TaskCandidatesRelation.class);

    private Tasks tasks;

	public void setTasks(Tasks tasks)
	{
		this.tasks = tasks;
	}

    /**
     * List the tasks candidate users and groups.
     * 
     * @see org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.Read#readAll(java.lang.String, org.alfresco.rest.framework.resource.parameters.Parameters)
     */
    @Override
    @WebApiDescription(title = "Get Task Candidate Users and Groups", description = "Get a paged list of the the task candidate users and groups")
    public CollectionWithPagingInfo<TaskCandidate> readAll(String taskId, Parameters parameters)
    {
        return tasks.getTaskCandidates(taskId, parameters.getPaging());
    }
}

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

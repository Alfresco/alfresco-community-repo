/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.task.Task;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * An {@link JobHandler} which executes activiti timer-jobs
 * authenticated against Alfresco. It runs the timer execution
 * as the task's assignee (if any) when the timer is applied to a
 * task. If not, system user is used to execute timer.
 * 
 * It wraps another JobHandler to which the actual execution is delegated to.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class AuthenticatedTimerJobHandler implements JobHandler 
{
	private JobHandler wrappedHandler;
	
	public AuthenticatedTimerJobHandler(JobHandler jobHandler) 
	{
		if(jobHandler == null)
		{
			throw new IllegalArgumentException("JobHandler to delegate to is required");
		}
		this.wrappedHandler = jobHandler;
	}
	
	@Override
	public void execute(final String configuration, final ExecutionEntity execution,
			final CommandContext commandContext) 
	{
		String userName = null;
		
		PvmActivity targetActivity = execution.getActivity();
		if(targetActivity != null)
		{
			// Only try getting active task, if execution timer is waiting on is a userTask
			String activityType = (String) targetActivity.getProperty(ActivitiConstants.NODE_TYPE);
			if(ActivitiConstants.USER_TASK_NODE_TYPE.equals(activityType))
			{
				Task task = new TaskQueryImpl(commandContext)
					.executionId(execution.getId())
					.executeSingleResult(commandContext);
				
				if(task != null && task.getAssignee() != null)
				{
					userName = task.getAssignee();
				}
			}
		}
		
		// When no task assignee is set, use system user to run job
		if(userName == null)
		{
			userName = AuthenticationUtil.getSystemUserName();
		}
		
		// Execute timer
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
            	wrappedHandler.execute(configuration, execution, commandContext);
            	return null;
            }
        }, userName);
	}	
	
	@Override
	public String getType() {
		return wrappedHandler.getType();
	}
}

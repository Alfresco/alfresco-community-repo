/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

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
    private NodeService unprotectedNodeService;
    
    /**
     * @param jobHandler the {@link JobHandler} to wrap.
     * @param nodeService the UNPROTECTED {@link NodeService} to use for fetching initiator username
     * when only tenant is known. We can't use initiator ScriptNode for this, because this uses the
     * protected {@link NodeService} which requires an authenticated user in that tenant (see {@link #getInitiator(ActivitiScriptNode)}).
     */
    public AuthenticatedTimerJobHandler(JobHandler jobHandler, NodeService nodeService) 
    {
        if (jobHandler == null)
        {
            throw new IllegalArgumentException("JobHandler to delegate to is required");
        }
        if(nodeService == null)
        {
            throw new IllegalArgumentException("NodeService is required");
        }
        this.unprotectedNodeService = nodeService;
        this.wrappedHandler = jobHandler;
    }
    
    @Override
    public void execute(final JobEntity job, final String configuration, final ExecutionEntity execution,
                final CommandContext commandContext) 
    {
        String userName = null;
        String tenantToRunIn = (String) execution.getVariable(ActivitiConstants.VAR_TENANT_DOMAIN);
        if(tenantToRunIn != null && tenantToRunIn.trim().length() == 0)
        {
            tenantToRunIn = null;
        }
        
        final ActivitiScriptNode initiatorNode = (ActivitiScriptNode) execution.getVariable(WorkflowConstants.PROP_INITIATOR);
        
        // Extracting the properties from the initiatornode should be done in correct tennant or as administrator, since we don't 
        // know who started the workflow yet (We can't access node-properties when no valid authentication context is set up).
        if(tenantToRunIn != null)
        {
            userName = TenantUtil.runAsTenant(new TenantRunAsWork<String>()
            {
                @Override
                public String doWork() throws Exception
                {
                    return getInitiator(initiatorNode);
                }
            }, tenantToRunIn);
        }
        else
        {
            // No tenant on worklfow, run as admin in default tenant
            userName = AuthenticationUtil.runAs(new RunAsWork<String>()
            {
                @SuppressWarnings("synthetic-access")
                public String doWork() throws Exception
                {
                    return getInitiator(initiatorNode);
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        
        // Fall back to task assignee, if no initiator is found
        if(userName == null)
        {
            PvmActivity targetActivity = execution.getActivity();
            if (targetActivity != null)
            {
                // Only try getting active task, if execution timer is waiting on is a userTask
                String activityType = (String) targetActivity.getProperty(ActivitiConstants.NODE_TYPE);
                if (ActivitiConstants.USER_TASK_NODE_TYPE.equals(activityType))
                {
                    Task task = new TaskQueryImpl(commandContext)
                    .executionId(execution.getId())
                    .executeSingleResult(commandContext);
                    
                    if (task != null && task.getAssignee() != null)
                    {
                        userName = task.getAssignee();
                    }
                }
            }
        }
        
        // When no task assignee is set, nor the initiator, use system user to run job
        if (userName == null)
        {
            userName = AuthenticationUtil.getSystemUserName();
            tenantToRunIn = null;
        }
        
        if(tenantToRunIn != null)
        {
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    wrappedHandler.execute(job, configuration, execution, commandContext);
                    return null;
                }
            }, userName, tenantToRunIn);
        }
        else
        {
            // Execute the timer without tenant
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @SuppressWarnings("synthetic-access")
                public Void doWork() throws Exception
                {
                    wrappedHandler.execute(job, configuration, execution, commandContext);
                    return null;
                }
            }, userName);
        }
    }
    
    protected String getInitiator(ActivitiScriptNode initiatorNode)
    {
        if(initiatorNode != null) 
        {
            NodeRef ref = initiatorNode.getNodeRef();
            if(unprotectedNodeService.exists(ref))
            {
                return (String) unprotectedNodeService.getProperty(ref, ContentModel.PROP_USERNAME);
            }
        }
        return null;
    }

    @Override
    public String getType() 
    {
        return wrappedHandler.getType();
    }
}

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

package org.alfresco.repo.workflow.activiti.listener;

import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * An {@link ExecutionListener} that set all additional variables that are needed 
 * when process starts.
 * 
 * @author Frederik Heremans
 * @since 4.0
 */
public class ProcessStartExecutionListener implements ExecutionListener
{

    private static final long serialVersionUID = 1L;
    protected TenantService tenantService;
    protected boolean deployWorkflowsInTenant;
    
    public void notify(DelegateExecution execution) throws Exception
    {
        // Add the workflow ID
        String instanceId = BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, execution.getId());
        execution.setVariable(WorkflowConstants.PROP_WORKFLOW_INSTANCE_ID, instanceId);

        if(tenantService.isEnabled() || !deployWorkflowsInTenant) 
        {
            // Add tenant as variable to the process. This will allow task-queries to filter out tasks that
            // are not part of the calling user's domain
            execution.setVariable(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
        }
        // MNT-9713
        if (execution instanceof ExecutionEntity)
        {
            ExecutionEntity exc = (ExecutionEntity) execution;
            if (exc.getSuperExecutionId() != null && exc.getVariable(ActivitiConstants.PROP_START_TASK_END_DATE) == null)
            {
                exc.setVariable(ActivitiConstants.PROP_START_TASK_END_DATE, new Date());
            }
        }
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setDeployWorkflowsInTenant(boolean deployWorkflowsInTenant)
    {
        this.deployWorkflowsInTenant = deployWorkflowsInTenant;
    }
}

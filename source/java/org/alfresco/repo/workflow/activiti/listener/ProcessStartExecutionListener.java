
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

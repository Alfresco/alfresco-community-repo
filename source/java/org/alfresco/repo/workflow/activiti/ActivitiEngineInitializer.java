package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bean that starts up the Activiti job executor as part of the
 * bootstrap process.
 *
 * @author Frederik Heremans
 * @since 4.0
 */
public class ActivitiEngineInitializer extends AbstractLifecycleBean
{
    private ProcessEngine processEngine;
    private WorkflowAdminService workflowAdminService;
    
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event) {
    	
    	this.processEngine = getApplicationContext().getBean(ProcessEngine.class);
    	
    	if (workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID)) 
    	{
    		((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getJobExecutor().start();
    	}
    }

	@Override
	protected void onShutdown(ApplicationEvent event) {
		if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID) && 
				((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getJobExecutor().isActive())
		{
			((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getJobExecutor().shutdown();
		}
	}
}

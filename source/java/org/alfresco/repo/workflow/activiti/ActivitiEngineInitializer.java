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

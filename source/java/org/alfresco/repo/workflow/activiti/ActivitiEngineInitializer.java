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
import org.alfresco.repo.domain.schema.SchemaAvailableEvent;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Class that waits for an {@link SchemaAvailableEvent} to start the activiti
 * job executor.
 *
 * @author Frederik Heremans
 */
public class ActivitiEngineInitializer implements ApplicationListener<ApplicationEvent>
{
    private ProcessEngine processEngine;
    private WorkflowAdminService workflowAdminService;
    
    public void setProcessEngine(ProcessEngine processEngine)
    {
        this.processEngine = processEngine;
    }
    
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof SchemaAvailableEvent && processEngine instanceof ProcessEngineImpl &&
            workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID)) 
        {
            // Start the job-executor
            ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getJobExecutor().start();
        }
    }
}

/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.jbpm;

import org.jbpm.job.executor.JobExecutor;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springmodules.workflow.jbpm31.JbpmTemplate;


/**
 * JBPM Scheduler
 * 
 * Manages lifecycle of Jbpm Job Executor.
 *
 * @author davidc
 */
public class JBPMScheduler extends AbstractLifecycleBean
{
    private JobExecutor executor = null; 
    private JbpmTemplate jbpmTemplate;
    private boolean JbpmEngineEnabled = false;
    
    /**
     * @param jbpmTemplate
     */
    public void setJBPMTemplate(JbpmTemplate jbpmTemplate)
    {
        this.jbpmTemplate = jbpmTemplate;
    }
    
    /**
     * @param jbpmEngineEnabled whether or not the JBPM-Engine is enables. Please note that we are
     * not using the WorklfowAdminService since this is only initialized later in the sequence.
     */
    public void setJBPMEngineEnabled(boolean jbpmEngineEnabled) {
		JbpmEngineEnabled = jbpmEngineEnabled;
	}
        
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
    	if(JbpmEngineEnabled)
    	{
    		executor = jbpmTemplate.getJbpmConfiguration().getJobExecutor();
    		executor.start();
    	}
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    	if(JbpmEngineEnabled && executor.isStarted())
    	{
    		executor.stop();
    	}
    }
    
}

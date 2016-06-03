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
     * @param jbpmTemplate JbpmTemplate
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

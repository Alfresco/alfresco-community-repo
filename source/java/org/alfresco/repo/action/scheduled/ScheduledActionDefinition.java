package org.alfresco.repo.action.scheduled;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.InitializingBean;

/**
 * The information needed to schedule a job.
 * 
 * The implementation is responsible for creating job details, triggers and registering with a scheduler.
 * 
 * This is not used anywhere at the moment. When we have a service then scheduled actions will be registered with the service.
 * 
 * @author Andy Hind
 */
public interface ScheduledActionDefinition extends InitializingBean
{
    /**
     * Set the template action definition that is used to build the Action to execute.
     * 
     * @param templateActionDefinition TemplateActionDefinition
     */
    public void setTemplateActionDefinition(TemplateActionDefinition templateActionDefinition);

    /**
     * Get the template action definition that is used to build the Action to execute.
     * 
     * @return - the template action definition.
     */
    public TemplateActionDefinition getTemplateActionDefinition();

    /**
     * Register with a scheduler. This should be called in the implementation afterPropertiesSet() method of InitializingBean
     * 
     * @param scheduler Scheduler
     * @throws SchedulerException
     */
    public void register(Scheduler scheduler) throws SchedulerException;

    /**
     * Set the name of the job - used for job admin 
     * 
     * @param jobName String
     */
    public void setJobName(String jobName);

    /**
     * Get the name of the job - used for job admin
     * 
     * @return - the job name
     */
    public String getJobName();

    /**
     * Set the job group - used for job admin
     * 
     * @param jobGroup String
     */
    public void setJobGroup(String jobGroup);

    /**
     * Get the job group - used for job admin
     * @return - the job group.
     */
    public String getJobGroup();

    /**
     * Set the trigger name - used for job admin
     * 
     * @param triggerName String
     */
    public void setTriggerName(String triggerName);

    /**
     * Get the trigger name - used for job admin
     * @return - the trigger name.
     */
    public String getTriggerName();

    /** 
     * Set the trigger group - used for job admin
     * 
     * @param triggerGroup String
     */
    public void setTriggerGroup(String triggerGroup);

    /**
     * Get the trigger group - used for job admin
     * 
     * @return - the trigger group.
     */
    public String getTriggerGroup();

}

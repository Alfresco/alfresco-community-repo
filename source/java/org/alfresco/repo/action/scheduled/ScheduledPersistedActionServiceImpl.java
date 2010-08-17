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
package org.alfresco.repo.action.scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * A service which handles the scheduling of the execution of persisted actions.
 * It handles registering them with the Quartz scheduler on repository start,
 * and handles the edit, creation and deletion of them.
 * 
 * @author Nick Burch
 * @since 3.4
 */
public class ScheduledPersistedActionServiceImpl implements ScheduledPersistedActionService
{
    protected static final String JOB_SCHEDULE_NODEREF = "ScheduleNodeRef";
    protected static final String JOB_ACTION_NODEREF = "ActionNodeRef";
    
    protected static NodeRef SCHEDULED_ACTION_ROOT_NODE_REF;

    protected static final Set<QName> ACTION_TYPES = new HashSet<QName>(Arrays
                .asList(new QName[] { ActionModel.TYPE_ACTION })); // TODO

    protected static final String SCHEDULER_GROUP = "PersistedActions";

    private static final Log log = LogFactory.getLog(ScheduledPersistedActionServiceImpl.class);

    private Scheduler scheduler;
    private NodeService nodeService;
    private NodeService startupNodeService;
    private RuntimeActionService runtimeActionService;
    private Repository repositoryHelper;

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the node service to use during startup, which won't do permissions
     * check etc
     */
    public void setStartupNodeService(NodeService startupNodeService)
    {
        this.startupNodeService = startupNodeService;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setRuntimeActionService(RuntimeActionService runtimeActionService) 
    {
        this.runtimeActionService = runtimeActionService;
    }
    

    protected void locatePersistanceFolder()
    {
        NodeRef dataDictionary = startupNodeService.getChildByName(
              repositoryHelper.getCompanyHome(),
              ContentModel.ASSOC_CONTAINS,
              "data dictionary"
        );
        SCHEDULED_ACTION_ROOT_NODE_REF = startupNodeService.getChildByName(
              dataDictionary,
              ContentModel.ASSOC_CONTAINS,
              "Scheduled Actions"
        );
    }
    
    /**
     * Find all our previously persisted scheduled actions, and tell the
     * scheduler to start handling them. Called by spring when startup is
     * complete.
     */
    public void schedulePreviouslyPersisted()
    {
        // Look up our persisted actions and schedule
        List<ScheduledPersistedAction> actions = listSchedules(startupNodeService);
        for (ScheduledPersistedAction action : actions)
        {
            addToScheduler((ScheduledPersistedActionImpl) action);
        }
    }

    /**
     * Creates a new schedule, for the specified Action.
     */
    public ScheduledPersistedAction createSchedule(Action persistedAction)
    {
        return new ScheduledPersistedActionImpl(persistedAction);
    }

    /**
     * Saves the changes to the schedule to the repository, and updates the
     * Scheduler with any changed details.
     */
    public void saveSchedule(ScheduledPersistedAction schedule)
    {
        // Remove if already there
        removeFromScheduler((ScheduledPersistedActionImpl) schedule);
        
        // TODO Create the node + relationship if not already there
        
        // Save to the repo
        // TODO Persist details
        
        // Add to the scheduler again 
        addToScheduler((ScheduledPersistedActionImpl) schedule);
    }

    /**
     * Removes the schedule for the action, and cancels future executions of it.
     * The persisted action is unchanged.
     */
    public void deleteSchedule(ScheduledPersistedAction schedule)
    {
        // Remove from the scheduler
        removeFromScheduler((ScheduledPersistedActionImpl) schedule);

        // Now remove from the repo
        // TODO
    }

    /**
     * Returns the schedule for the specified action, or null if it isn't
     * currently scheduled.
     */
    public ScheduledPersistedAction getSchedule(Action persistedAction)
    {
        // TODO look for a relationship of the special type from
        //  the action, then if we find it load the schedule via it
        return null;
    }

    /**
     * Returns all currently scheduled actions.
     */
    public List<ScheduledPersistedAction> listSchedules()
    {
        return listSchedules(nodeService);
    }

    private List<ScheduledPersistedAction> listSchedules(NodeService nodeService)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                    SCHEDULED_ACTION_ROOT_NODE_REF, ACTION_TYPES);

        List<ScheduledPersistedAction> scheduledActions = new ArrayList<ScheduledPersistedAction>(
                    childAssocs.size());
        for (ChildAssociationRef actionAssoc : childAssocs)
        {
            // TODO
            // Action nextAction =
            // runtimeActionService.createAction(actionAssoc.getChildRef());
            // renderingActions.add(new ReplicationDefinitionImpl(nextAction));
        }

        return scheduledActions;
    }

    /**
     * Takes an entry out of the scheduler, if it's currently there.
     */
    protected void removeFromScheduler(ScheduledPersistedActionImpl schedule)
    {
        // Jobs are indexed by the persisted node ref
        // So, only try to remove if persisted
        if(schedule.getPersistedAtNodeRef() == null) 
           return;
       
        // Ask to remove it
        try
        {
            scheduler.deleteJob(schedule.getPersistedAtNodeRef().toString(), SCHEDULER_GROUP);
        }
        catch (SchedulerException e)
        {
            // Probably means scheduler is shutting down
            log.warn(e);
        }
    }

    /**
     * Adds a new entry into the scheduler
     */
    protected void addToScheduler(ScheduledPersistedActionImpl schedule)
    {
        // Wrap it up in Quartz bits
        JobDetail details = buildJobDetail(schedule);
        Trigger trigger = schedule.asTrigger();

        // Schedule it
        try
        {
            scheduler.scheduleJob(details, trigger);
        }
        catch (SchedulerException e)
        {
            // Probably means scheduler is shutting down
            log.warn(e);
        }
    }

    protected JobDetail buildJobDetail(ScheduledPersistedActionImpl schedule)
    {
        // Build the details
        JobDetail detail = new JobDetail(schedule.getPersistedAtNodeRef().toString(),
                    SCHEDULER_GROUP, 
                    ScheduledJobWrapper.class
        );

        // Record the action that is to be executed
        detail.getJobDataMap().put(
              JOB_ACTION_NODEREF,
              schedule.getActionNodeRef().toString()
        );
        detail.getJobDataMap().put(
              JOB_SCHEDULE_NODEREF,
              schedule.getPersistedAtNodeRef().toString()
        );
        
        // All done
        return detail;
    }
    
    
    /**
     * This is used to trigger the loading of previously persisted schedules on
     *  an application startup. It is an additional bean to make the context
     *  files cleaner.
     */
    public static class ScheduledPersistedActionServiceBootstrap extends AbstractLifecycleBean
    {
       private ScheduledPersistedActionServiceImpl service;
       public void setScheduledPersistedActionService(ScheduledPersistedActionServiceImpl scheduledPersistedActionService)
       {
          this.service = scheduledPersistedActionService;
       }
       
       public void onBootstrap(ApplicationEvent event)
       {
          service.locatePersistanceFolder();
          service.schedulePreviouslyPersisted();
       }
       
       public void onShutdown(ApplicationEvent event)
       {
          // We don't need to do anything here, as the scheduler shutdown
          //  will stop running our jobs for us
       }
    }
    
    /**
     * The thing that Quartz runs when the schedule fires.
     * Handles fetching the action, and having it run asynchronously
     */
    public static class ScheduledJobWrapper implements Job, ApplicationContextAware
    {
       private ActionService actionService;
       private RuntimeActionService runtimeActionService;
       
       public void setApplicationContext(ApplicationContext applicationContext)
       {
          actionService = (ActionService)applicationContext.getBean("ActionService");
          runtimeActionService = (RuntimeActionService)applicationContext.getBean("actionService");
       }

       public void execute(JobExecutionContext jobContext)
       {
          // Do all this work as system
          // TODO - See if we can pinch some bits from the existing scheduled
          //  actions around who to run as
          AuthenticationUtil.setRunAsUserSystem();
          
          // Create the action object
          NodeRef actionNodeRef = new NodeRef(
                jobContext.getMergedJobDataMap().getString(JOB_ACTION_NODEREF)
          );
          Action action = runtimeActionService.createAction(
                actionNodeRef
          );
          
          // Have it executed asynchronously
          actionService.executeAction(
                action, (NodeRef)null,
                false, true
          );
       }
    }
}

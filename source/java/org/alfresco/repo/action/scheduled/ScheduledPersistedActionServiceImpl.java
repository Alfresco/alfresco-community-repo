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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.SchedulableAction.IntervalPeriod;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
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
    
    protected NodeRef SCHEDULED_ACTION_ROOT_NODE_REF;

    protected static final Set<QName> ACTION_TYPES = new HashSet<QName>(Arrays
                .asList(new QName[] { ActionModel.TYPE_ACTION_SCHEDULE }));

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
        // TODO: Use SearchService.selectNodes(repositoryHelper.getCompanyHome(), "/app:dictionary/Scheduled Actions");
        //       Log error if result not found
        //       Log warning if multiple results found
        List<ChildAssociationRef> dictionaryAssocs = startupNodeService.getChildAssocs(
                repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"));
        if (dictionaryAssocs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Failed to find 'app:dictionary' node");
        }
        NodeRef dataDictionary = dictionaryAssocs.get(0).getChildRef();
        List<ChildAssociationRef> scheduledAssocs = startupNodeService.getChildAssocs(
                dataDictionary, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Scheduled Actions"));
        if (scheduledAssocs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Failed to find 'cm:Scheduled Actions' location.");
        }
        SCHEDULED_ACTION_ROOT_NODE_REF = scheduledAssocs.get(0).getChildRef();
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
            // Only schedule if the action still exists
            if(action.getActionNodeRef() != null)
            {
               addToScheduler((ScheduledPersistedActionImpl) action);
            }
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
        ScheduledPersistedActionImpl scheduleImpl = (ScheduledPersistedActionImpl)schedule;
        
        // Remove if already there
        removeFromScheduler(scheduleImpl);
        
        if (scheduleImpl.getPersistedAtNodeRef() == null)
        {
            // if not already persisted, create the persistent schedule
            createPersistentSchedule(scheduleImpl);
        }
        
        // update the persistent schedule with schedule properties
        updatePersistentSchedule(scheduleImpl);

        // Add to the scheduler again 
        addToScheduler(scheduleImpl);
    }

    private void createPersistentSchedule(ScheduledPersistedActionImpl schedule)
    {
        ChildAssociationRef childAssoc = nodeService.createNode(SCHEDULED_ACTION_ROOT_NODE_REF,
                ContentModel.ASSOC_CONTAINS, QName.createQName(GUID.generate()), 
                ActionModel.TYPE_ACTION_SCHEDULE); 
        schedule.setPersistedAtNodeRef(childAssoc.getChildRef());
    }

    private void updatePersistentSchedule(ScheduledPersistedActionImpl schedule)
    {
        NodeRef nodeRef = schedule.getPersistedAtNodeRef();
        if (nodeRef == null)
            throw new IllegalStateException("Must be persisted first");
        
        // update schedule properties
        nodeService.setProperty(nodeRef, ActionModel.PROP_START_DATE, schedule.getScheduleStart());
        nodeService.setProperty(nodeRef, ActionModel.PROP_INTERVAL_COUNT, schedule.getScheduleIntervalCount());
        IntervalPeriod period = schedule.getScheduleIntervalPeriod();
        nodeService.setProperty(nodeRef, ActionModel.PROP_INTERVAL_PERIOD, period == null ? null : period.name());
        
        // We don't save the last executed at date here, that only gets changed
        //  from within the execution loop
        
        // update scheduled action (represented as an association)
        // NOTE: can only associate to a single action from a schedule (as specified by the action model)
        
        // update association to reflect updated schedule
        AssociationRef actionAssoc = findActionAssociationFromSchedule(nodeRef);
        NodeRef actionNodeRef = schedule.getActionNodeRef();
        if (actionNodeRef == null)
        {
            if (actionAssoc != null)
            {
                // remove associated action
                nodeService.removeAssociation(actionAssoc.getSourceRef(), actionAssoc.getTargetRef(), actionAssoc.getTypeQName());
            }
        }
        else
        {
            if (actionAssoc == null)
            {
                // create associated action
                nodeService.createAssociation(nodeRef, actionNodeRef, ActionModel.ASSOC_SCHEDULED_ACTION);
            }
            else if (!actionAssoc.getTargetRef().equals(actionNodeRef))
            {
                // associated action has changed... first remove existing association
                nodeService.removeAssociation(actionAssoc.getSourceRef(), actionAssoc.getTargetRef(), actionAssoc.getTypeQName());
                nodeService.createAssociation(nodeRef, actionNodeRef, ActionModel.ASSOC_SCHEDULED_ACTION);
            }
        }
    }
    
    /**
     * Removes the schedule for the action, and cancels future executions of it.
     * The persisted action is unchanged.
     */
    public void deleteSchedule(ScheduledPersistedAction schedule)
    {
        ScheduledPersistedActionImpl scheduleImpl = (ScheduledPersistedActionImpl)schedule;
        // Remove from the scheduler
        removeFromScheduler(scheduleImpl);

        // Now remove from the repo
        deletePersistentSchedule(scheduleImpl);
    }

    private void deletePersistentSchedule(ScheduledPersistedActionImpl schedule)
    {
        NodeRef nodeRef = schedule.getPersistedAtNodeRef();
        if (nodeRef == null)
            return;

        // NOTE: this will also cascade delete action association
        nodeService.deleteNode(nodeRef);
        
        schedule.setPersistedAtNodeRef(null);
    }
    
    /**
     * Returns the schedule for the specified action, or null if it isn't
     * currently scheduled.
     */
    public ScheduledPersistedAction getSchedule(Action persistedAction)
    {
        NodeRef nodeRef = persistedAction.getNodeRef();
        if (nodeRef == null)
        {
            // action is not persistent
            return null;
        }

        // locate associated schedule for action
        List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        AssociationRef scheduledAssoc = null;
        for (AssociationRef assoc : assocs)
        {
            if (ActionModel.ASSOC_SCHEDULED_ACTION.equals(assoc.getTypeQName()))
            {
                scheduledAssoc = assoc;
                break;
            }
        }
        
        if (scheduledAssoc == null)
        {
            // there is no associated schedule
            return null;
        }
        
        // load the scheduled action
        return loadPersistentSchedule(scheduledAssoc.getSourceRef());
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
            ScheduledPersistedActionImpl scheduleImpl = loadPersistentSchedule(actionAssoc.getChildRef());
            scheduledActions.add(scheduleImpl);
        }

        return scheduledActions;
    }

    protected ScheduledPersistedActionImpl loadPersistentSchedule(NodeRef schedule)
    {
        if (!nodeService.exists(schedule))
            return null;
           
        // create action
        Action action = null;
        AssociationRef actionAssoc = findActionAssociationFromSchedule(schedule);
        if (actionAssoc != null)
        {
            action = runtimeActionService.createAction(actionAssoc.getTargetRef());
        }
        
        // create schedule
        ScheduledPersistedActionImpl scheduleImpl = new ScheduledPersistedActionImpl(action);
        scheduleImpl.setPersistedAtNodeRef(schedule);
        
        scheduleImpl.setScheduleLastExecutedAt((Date)nodeService.getProperty(schedule, ActionModel.PROP_LAST_EXECUTED_AT));
        
        scheduleImpl.setScheduleStart((Date)nodeService.getProperty(schedule, ActionModel.PROP_START_DATE));
        scheduleImpl.setScheduleIntervalCount((Integer)nodeService.getProperty(schedule, ActionModel.PROP_INTERVAL_COUNT));
        String period = (String)nodeService.getProperty(schedule, ActionModel.PROP_INTERVAL_PERIOD);
        if (period != null)
        {
            scheduleImpl.setScheduleIntervalPeriod(IntervalPeriod.valueOf(period));
        }
        
        return scheduleImpl;
    }
    
    private AssociationRef findActionAssociationFromSchedule(NodeRef schedule)
    {
        List<AssociationRef> assocs = nodeService.getTargetAssocs(schedule, RegexQNamePattern.MATCH_ALL);
        AssociationRef actionAssoc = null;
        for (AssociationRef assoc : assocs)
        {
            if (ActionModel.ASSOC_SCHEDULED_ACTION.equals(assoc.getTypeQName()))
            {
                actionAssoc = assoc;
                break;
            }
        }
        
        return actionAssoc;
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
     * Builds up the Quartz details, and adds it to the Quartz
     *  scheduler when the transaction completes.
     * We have to wait for the transaction to finish, otherwise
     *  Quartz may end up trying and failing to load the details
     *  of a job that hasn't been committed to the repo yet!
     */
    protected void addToScheduler(ScheduledPersistedActionImpl schedule)
    {
        // Wrap it up in Quartz bits
        final JobDetail details = buildJobDetail(schedule);
        final Trigger trigger = schedule.asTrigger();

        // As soon as the transaction commits, add it
        AlfrescoTransactionSupport.bindListener(
           new TransactionListenerAdapter() {
               @Override
               public void afterCommit() {
                  // Schedule it with Quartz
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
           }
        );
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
       private RetryingTransactionHelper txnHelper;
       
       public void setScheduledPersistedActionService(ScheduledPersistedActionServiceImpl scheduledPersistedActionService)
       {
          this.service = scheduledPersistedActionService;
       }
       
       public void setTransactionHelper(RetryingTransactionHelper txnHelper)
       {
           this.txnHelper = txnHelper;
       }

       public void onBootstrap(ApplicationEvent event)
       {
           AuthenticationUtil.runAs(new RunAsWork<Object>()
           {
               public Object doWork()
               {
                   RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
                   {
                       public Object execute() throws Throwable
                       {
                           service.locatePersistanceFolder();
                           service.schedulePreviouslyPersisted();
                           return null;
                       }
                   };
                   return txnHelper.doInTransaction(callback);
               }
           }, AuthenticationUtil.getSystemUserName());
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
       private NodeService nodeService;
       private TransactionService transactionService;
       private RuntimeActionService runtimeActionService;
       
       public void setApplicationContext(ApplicationContext applicationContext)
       {
          nodeService = (NodeService)applicationContext.getBean("NodeService");
          actionService = (ActionService)applicationContext.getBean("ActionService");
          transactionService = (TransactionService)applicationContext.getBean("transactionService");
          runtimeActionService = (RuntimeActionService)applicationContext.getBean("actionService");
       }

       public void execute(final JobExecutionContext jobContext)
       {
          // Do all this work as system
          // TODO - See if we can pinch some bits from the existing scheduled
          //  actions around who to run as
          AuthenticationUtil.setRunAsUserSystem();
          
          transactionService.getRetryingTransactionHelper().doInTransaction(
             new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {
                  // Update the last run time on the schedule
                  NodeRef scheduleNodeRef = new NodeRef(
                        jobContext.getMergedJobDataMap().getString(JOB_SCHEDULE_NODEREF)
                  );
                  nodeService.setProperty(
                         scheduleNodeRef, 
                         ActionModel.PROP_LAST_EXECUTED_AT, 
                         new Date()
                  );
                  
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
                  
                  // Real work starts when the transaction completes
                  return null;
               }
             }, false, true
          );
       }
    }
}

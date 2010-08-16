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

import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * A service which handles the scheduling of the
 *  execution of persisted actions.
 * It handles registering them with the Quartz
 *  scheduler on repository start, and handles
 *  the edit, creation and deletion of them.
 * 
 * @author Nick Burch
 * @since 3.4
 */
public class ScheduledPersistedActionServiceImpl implements ScheduledPersistedActionService {
   protected static final String SCHEDULED_ACTION_ROOT_PATH = 
      "/app:company_home/app:dictionary/cm:Scheduled_x0020_Actions";
   protected static NodeRef SCHEDULED_ACTION_ROOT_NODE_REF;
   protected static final Set<QName> ACTION_TYPES = new HashSet<QName>(
         Arrays.asList(new QName[] { ActionModel.TYPE_ACTION })); // TODO
   
   protected static final String SCHEDULER_GROUP = "PersistedActions";
   
   private static final Log log = LogFactory.getLog(ScheduledPersistedActionServiceImpl.class);
   
   private Scheduler scheduler;
   private NodeService nodeService;
   private NodeService startupNodeService;
   private ActionService actionService;
   private SearchService startupSearchService;
   private NamespaceService namespaceService;
   private RuntimeActionService runtimeActionService;
   
   public void setScheduler(Scheduler scheduler) 
   {
      this.scheduler = scheduler;
   }

   public void setNodeService(NodeService nodeService) 
   {
      this.nodeService = nodeService;
   }

   /**
    * Sets the node service to use during startup, which
    *  won't do permissions check etc
    */
   public void setStartupNodeService(NodeService startupNodeService) 
   {
      this.startupNodeService = startupNodeService;
   }
   
   public void setStartupSearchService(SearchService startupSearchService)
   {
      this.startupSearchService = startupSearchService;
   }

   public void setActionService(ActionService actionService) 
   {
      this.actionService = actionService;
   }

   public void setRuntimeActionService(RuntimeActionService runtimeActionService) 
   {
      this.runtimeActionService = runtimeActionService;
   }
   
   public void setNamespaceService(NamespaceService namespaceService) 
   {
      this.namespaceService = namespaceService;
   }
   

   /**
    * Find all our previously persisted scheduled actions, and
    *  tell the scheduler to start handling them.
    * Called by spring when startup is complete.
    */
   public void schedulePreviouslyPersisted() {
      // Grab the path of our bit of the data dictionary
      StoreRef spacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
      List<NodeRef> nodes = startupSearchService.selectNodes(
            startupNodeService.getRootNode(spacesStore),
            SCHEDULED_ACTION_ROOT_PATH,
            null, namespaceService, false
      );
      if(nodes.size() != 1)
      {
         throw new IllegalStateException("Tries to find the Scheduled Actions Data Dictionary" +
               " folder at " + SCHEDULED_ACTION_ROOT_PATH + " but got " + nodes.size() + "results");
      }
      SCHEDULED_ACTION_ROOT_NODE_REF = nodes.get(0);
            
      // Now, look up our persisted actions and schedule
      List<ScheduledPersistedAction> actions = listSchedules(startupNodeService);
      for(ScheduledPersistedAction action : actions)
      {
         addToScheduler((ScheduledPersistedActionImpl)action);
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
    * Saves the changes to the schedule to the repository,
    *  and updates the Scheduler with any changed details.
    */
   public void saveSchedule(ScheduledPersistedAction schedule)
   {
      removeFromScheduler((ScheduledPersistedActionImpl)schedule);
      addToScheduler((ScheduledPersistedActionImpl)schedule);
      
      // TODO
   }
   
   /**
    * Removes the schedule for the action, and cancels future
    *  executions of it.
    * The persisted action is unchanged.
    */
   public void deleteSchedule(ScheduledPersistedAction schedule)
   {
      removeFromScheduler((ScheduledPersistedActionImpl)schedule);
      
      // TODO
   }
   
   /**
    * Returns the schedule for the specified action, or
    *  null if it isn't currently scheduled. 
    */
   public ScheduledPersistedAction getSchedule(Action persistedAction)
   {
      // TODO
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
      List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(SCHEDULED_ACTION_ROOT_NODE_REF, ACTION_TYPES);

      List<ScheduledPersistedAction> scheduledActions = new ArrayList<ScheduledPersistedAction>(childAssocs.size());
      for (ChildAssociationRef actionAssoc : childAssocs)
      {
          // TODO
//          Action nextAction = runtimeActionService.createAction(actionAssoc.getChildRef());
//          renderingActions.add(new ReplicationDefinitionImpl(nextAction));
      }

      return scheduledActions;
   }
   
   
   /**
    * Takes an entry out of the scheduler, if it's currently
    *  there.
    */
   protected void removeFromScheduler(ScheduledPersistedActionImpl schedule)
   {
      // Jobs are indexed by the persisted node ref
      try {
         scheduler.deleteJob(
               schedule.getPersistedAtNodeRef().toString(),
               SCHEDULER_GROUP
         );
      } catch (SchedulerException e) {
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
      try {
         scheduler.scheduleJob(details, trigger);
      } catch (SchedulerException e) {
         // Probably means scheduler is shutting down 
         log.warn(e);
      } 
   }

   protected JobDetail buildJobDetail(ScheduledPersistedActionImpl schedule)
   {
      JobDetail detail = new JobDetail(
            schedule.getPersistedAtNodeRef().toString(),
            SCHEDULER_GROUP,
            null // TODO
      );
      
      // TODO
      
      return detail;
   }
}

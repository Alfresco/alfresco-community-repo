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
package org.alfresco.repo.web.scripts.replication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.action.AbstractActionWebscript;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ISO8601DateFormat;

/**
 * Builds up models from ReplicationDefinitions, either
 *  in summary or detail form.
 *  
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationModelBuilder 
{
    protected static final String MODEL_DATA_ITEM = "replicationDefinition";
    protected static final String MODEL_DATA_LIST = "replicationDefinitions";
    
    protected static final String DEFINITION_NAME = "name";
    protected static final String DEFINITION_DESCRIPTION = "description";
    protected static final String DEFINITION_STATUS = "status";
    protected static final String DEFINITION_STARTED_AT = "startedAt";
    protected static final String DEFINITION_ENDED_AT = "endedAt";
    protected static final String DEFINITION_FAILURE_MESSAGE = "failureMessage";
    protected static final String DEFINITION_RUNNING_ACTION_ID = "runningActionId";
    protected static final String DEFINITION_PAYLOAD = "payload";
    protected static final String DEFINITION_TRANSFER_LOCAL_REPORT = "transferLocalReport";
    protected static final String DEFINITION_TRANSFER_REMOTE_REPORT = "transferRemoteReport";
    protected static final String DEFINITION_ENABLED = "enabled";
    protected static final String DEFINITION_TARGET_NAME = "targetName";
    
    protected static final String DEFINITION_SCHEDULE_ENABLED = "scheduleEnabled";
    protected static final String DEFINITION_SCHEDULE_START  = "scheduleStart";
    protected static final String DEFINITION_SCHEDULE_PERIOD = "scheduleIntervalPeriod";
    protected static final String DEFINITION_SCHEDULE_COUNT  = "scheduleIntervalCount";
    
    protected NodeService nodeService;
    protected ReplicationService replicationService;
    protected ActionTrackingService actionTrackingService;

    public ReplicationModelBuilder(NodeService nodeService, ReplicationService replicationService,
                                   ActionTrackingService actionTrackingService) 
    {
       this.nodeService = nodeService;
       this.replicationService = replicationService;
       this.actionTrackingService = actionTrackingService;
    }
    
    
    /**
     * Sorts simple definitions by their status
     */
    public static class SimpleSorterByStatus implements Comparator<Map<String,Object>> {
      private SimpleSorterByName nameSorter = new SimpleSorterByName();
      public int compare(Map<String, Object> simpleA, Map<String, Object> simpleB) {
         String statusA = (String)simpleA.get(DEFINITION_STATUS);
         String statusB = (String)simpleB.get(DEFINITION_STATUS);
         if(statusA == null || statusB == null) {
            throw new IllegalArgumentException("Status missing during sort");
         }
         
         int compare = statusA.compareTo(statusB);
         if(compare == 0) {
            return nameSorter.compare(simpleA, simpleB);
         }
         return compare;
      }       
    }
    /**
     * Sorts simple definitions by their name
     */
    public static class SimpleSorterByName implements Comparator<Map<String,Object>> {
      public int compare(Map<String, Object> simpleA, Map<String, Object> simpleB) {
         String nameA = (String)simpleA.get(DEFINITION_NAME);
         String nameB = (String)simpleB.get(DEFINITION_NAME);
         if(nameA == null || nameB == null) {
            throw new IllegalArgumentException("Name missing during sort");
         }
         return nameA.compareTo(nameB);
      }       
    }
    /**
     * Sorts simple definitions by their last run time.
     * Things that have never been run go to the bottom of the list,
     *  so we see most recently run, least recently run, never run.
     */
    public static class SimpleSorterByLastRun implements Comparator<Map<String,Object>> {
      /** Works on ISO8601 formatted date strings */
      public int compare(Map<String, Object> simpleA, Map<String, Object> simpleB) {
         String dateA = (String)simpleA.get(DEFINITION_STARTED_AT);
         String dateB = (String)simpleB.get(DEFINITION_STARTED_AT);
         if(dateA == null && dateB == null) {
            return 0;
         }
         if(dateA != null && dateB == null) {
            return -1;
         }
         if(dateA == null && dateB != null) {
            return 1;
         }
         // We want more recent dates first
         return 0-dateA.compareTo(dateB);
      }       
    }
    
    
    /**
     * Build a model containing a list of simple definitions for the given
     *  list of Replication Definitions.
     */
    protected Map<String,Object> buildSimpleList(List<ReplicationDefinition> replicationDefinitions,
                                                 Comparator<Map<String,Object>> sorter)
    {
        List<Map<String,Object>> models = new ArrayList<Map<String,Object>>();
        
        // Only bother looking up the execution status if we
        //  have some Replication Definitions to render
        if(replicationDefinitions.size() > 0) {
           List<ExecutionSummary> executing =
              actionTrackingService.getExecutingActions(replicationDefinitions.get(0).getActionDefinitionName());
           
           for(ReplicationDefinition rd : replicationDefinitions) {
              // Get the executing detail(s) for this definition
              ExecutionDetails details = getExecutionDetails(rd, executing);
              
              // Set the core details
              Map<String, Object> rdm = new HashMap<String,Object>();
              rdm.put(DEFINITION_NAME, rd.getReplicationName());
              rdm.put(DEFINITION_ENABLED, rd.isEnabled()); 
              
              // Do the status
              setStatus(rd, details, rdm);
              // In the summary form, we don't need end time or details
              rdm.remove(DEFINITION_ENDED_AT);
              rdm.remove(DEFINITION_RUNNING_ACTION_ID);
              
              // Add to the list of finished models
              models.add(rdm);
           }
        }
        
        // Sort the entries
        if(sorter != null) {
           Collections.sort(models, sorter);
        }
        
        // Finish up
        Map<String, Object> model = new HashMap<String,Object>();
        model.put(MODEL_DATA_LIST, models);
        return model;
    }
    
    
    /**
     * Build a model containing the full, detailed definition for the given
     *  Replication Definition.
     */
    protected Map<String, Object> buildDetails(ReplicationDefinition rd) {
       Map<String, Object> rdm = new HashMap<String,Object>();
       
       // Set the core details
       rdm.put(DEFINITION_NAME, rd.getReplicationName());
       rdm.put(DEFINITION_DESCRIPTION, rd.getDescription());
       rdm.put(DEFINITION_ENABLED, rd.isEnabled()); 
       rdm.put(DEFINITION_TARGET_NAME, rd.getTargetName());
       
       // Set the scheduling details
       rdm.put(DEFINITION_SCHEDULE_ENABLED, rd.isSchedulingEnabled());
       if(rd.isSchedulingEnabled())
       {
          rdm.put(DEFINITION_SCHEDULE_START, ISO8601DateFormat.format(rd.getScheduleStart()));
          
          rdm.put(DEFINITION_SCHEDULE_COUNT, rd.getScheduleIntervalCount());
          if(rd.getScheduleIntervalPeriod() != null) {
             rdm.put(DEFINITION_SCHEDULE_PERIOD, rd.getScheduleIntervalPeriod().toString());
          } else {
             rdm.put(DEFINITION_SCHEDULE_PERIOD, null);
          }
       }
       
       // Set the details of the previous run
       // These will be null'd out later if replication is in progress
       rdm.put(DEFINITION_FAILURE_MESSAGE, rd.getExecutionFailureMessage());
       rdm.put(DEFINITION_TRANSFER_LOCAL_REPORT, rd.getLocalTransferReport());
       rdm.put(DEFINITION_TRANSFER_REMOTE_REPORT, rd.getRemoteTransferReport());
       
       // Do the status
       // Includes start+end times, and running action details
       setStatus(rd, rdm);
       
       // Only include the payload entries that still exist
       // Otherwise the freemarker layer gets upset about deleted nodes
       List<NodeRef> payload = new ArrayList<NodeRef>();
       for(NodeRef node : rd.getPayload()) {
          if(nodeService.exists(node))
             payload.add(node);
       }
       rdm.put(DEFINITION_PAYLOAD, payload);
       
       // Save in the usual way
       Map<String, Object> model = new HashMap<String,Object>();
       model.put(MODEL_DATA_ITEM, rdm);
       return model;
    }
    

    /**
     * Figures out the status that's one of:
     *    New|Running|CancelRequested|Completed|Failed|Cancelled
     * by merging data from the action tracking service. 
     * Will also set the start and end dates, from either the
     *  replication definition or action tracking data, depending
     *  on the status.
     */
    protected void setStatus(ReplicationDefinition replicationDefinition, Map<String, Object> model)
    {
        // Grab the running instance(s) of the action
        List<ExecutionSummary> executing =
           actionTrackingService.getExecutingActions(replicationDefinition);
        // Now get the details of that
        ExecutionDetails details = getExecutionDetails(replicationDefinition, executing);
        // Finally have the status set
        setStatus(replicationDefinition, details, model);
    }
    /**
     * Figures out the status that's one of:
     *    New|Running|CancelRequested|Completed|Failed|Cancelled
     * by merging data from the action tracking service. 
     * Will also set the start and end dates, from either the
     *  replication definition or action tracking data, depending
     *  on the status.
     */
    protected void setStatus(ReplicationDefinition replicationDefinition, 
                             ExecutionDetails details, Map<String, Object> model)
    {
        // Is it currently running?
        if(details == null) {
           // It isn't running, we can use the persisted details
           model.put(DEFINITION_STATUS, replicationDefinition.getExecutionStatus().toString());
           
           Date startDate = replicationDefinition.getExecutionStartDate();
           if(startDate != null) {
              model.put(DEFINITION_STARTED_AT, ISO8601DateFormat.format(startDate));
           } else {
              model.put(DEFINITION_STARTED_AT, null);
           }
           
           Date endDate = replicationDefinition.getExecutionEndDate();
           if(endDate != null) {
              model.put(DEFINITION_ENDED_AT, ISO8601DateFormat.format(endDate));
           } else {
              model.put(DEFINITION_ENDED_AT, null);
           }
           
           // It isn't running
           model.put(DEFINITION_RUNNING_ACTION_ID, null);
           
           return;
        }

        // As it is running / about to run, return the
        //  details of the running/pending version
        if(details.isCancelRequested()) {
           model.put(DEFINITION_STATUS, "CancelRequested");
        } else if(details.getStartedAt() == null) {
           model.put(DEFINITION_STATUS, "Pending");
        } else {
           model.put(DEFINITION_STATUS, "Running");
        }
        
        if(details.getStartedAt() != null) {
           model.put(DEFINITION_STARTED_AT, ISO8601DateFormat.format(details.getStartedAt()));
        } else {
           model.put(DEFINITION_STARTED_AT, null);
        }
        model.put(DEFINITION_ENDED_AT, null);
        model.put(DEFINITION_RUNNING_ACTION_ID, 
              AbstractActionWebscript.getRunningId(details.getExecutionSummary()));
        
        // Since it's running / about to run, there shouldn't
        //  be failure messages or transfer reports
        // If these currently exist on the model, remove them
        if(model.containsKey(DEFINITION_FAILURE_MESSAGE))
           model.put(DEFINITION_FAILURE_MESSAGE, null);
        if(model.containsKey(DEFINITION_TRANSFER_LOCAL_REPORT))
           model.put(DEFINITION_TRANSFER_LOCAL_REPORT, null);
        if(model.containsKey(DEFINITION_TRANSFER_REMOTE_REPORT))
           model.put(DEFINITION_TRANSFER_REMOTE_REPORT, null);
    }

    /**
     * For the given Replication Definition, and list of executing
     *  actions (which may or may not be only for this definition),
     *  return a single execution details. 
     *  
     * Returns null if no copies of the definition are executing.
     * Returns a predictable instance if more than one copy is
     *  executing.
     */
    private ExecutionDetails getExecutionDetails(ReplicationDefinition replicationDefinition,
                                                 List<ExecutionSummary> executing)
    {
       // Figure out which of the running actions are us
       List<ExecutionSummary> ours = new ArrayList<ExecutionSummary>();
       for(ExecutionSummary es : executing) {
          if(es.getActionType().equals(replicationDefinition.getActionDefinitionName()) &&
             es.getActionId().equals(replicationDefinition.getId())) {
             ours.add(es);
          }
       }
       
       // Do we have anything running at the moment
       if(ours.size() == 0) {
          // Not executing at the moment
          return null;
       }
       
       // We have at least one copy running
       ExecutionSummary es;
       if(executing.size() == 1) {
          // Only one copy, life is simple
          es = ours.get(0);
       } else {
          // More than one copy runing, joy
          // Go for the lowest execution instance id, so
          //  we're predictable
          es = ours.get(0);
          for(ExecutionSummary e : ours) {
             if(e.getExecutionInstance() < es.getExecutionInstance()) {
                es = e;
             }
          }
       }
       
       // Grab the details
       return actionTrackingService.getExecutionDetails(es);    
   }
}
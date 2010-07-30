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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Builds up models from ReplicationDefinitions, either
 *  in summary or detail form.
 *  
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationModelBuilder 
{
    protected static final String MODEL_DATA_LIST = "replicationDefinitions";
    
    protected static final String DEFINITION_NAME = "name";
    protected static final String DEFINITION_STATUS = "status";
    protected static final String DEFINITION_STARTED_AT = "startedAt";
    protected static final String DEFINITION_ENDED_AT = "endedAt";
    protected static final String DEFINITION_ENABLED = "enabled";
    protected static final String DEFINITION_DETAILS_URL = "details";
    
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
     * Build a model containing a list of simple definitions for the given
     *  list of Replication Definitions.
     */
    protected Map<String,Object> buildSimpleList(List<ReplicationDefinition> replicationDefinitions)
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
              rdm.put(DEFINITION_DETAILS_URL, buildDefinitionDetailsUrl(rd));
              
              // Do the status
              setStatus(rd, details, rdm);
              
              // Add to the list of finished models
              models.add(rdm);
           }
        }
        
        // Finish up
        Map<String, Object> model = new HashMap<String,Object>();
        model.put(MODEL_DATA_LIST, models);
        return model;
    }
    

    protected String buildDefinitionDetailsUrl(ReplicationDefinition replicationDefinition)
    {
       return "/api/replication-definition/" + replicationDefinition.getReplicationName();
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
           model.put(DEFINITION_STARTED_AT, replicationDefinition.getExecutionStartDate());
           model.put(DEFINITION_ENDED_AT,   replicationDefinition.getExecutionEndDate());
           return;
        }
        
        // Use the details of the running copy
        if(details.isCancelRequested()) {
           model.put(DEFINITION_STATUS, "CancelRequested");
        } else {
           model.put(DEFINITION_STATUS, "Running");
        }
        model.put(DEFINITION_STARTED_AT, details.getStartedAt());
        model.put(DEFINITION_ENDED_AT, null);
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
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

import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionTrackingServiceImpl;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public abstract class AbstractReplicationWebscript extends DeclarativeWebScript
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
    
    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionTrackingService(ActionTrackingService actionTrackingService) 
    {
        this.actionTrackingService = actionTrackingService;
    }
    
    protected String getDefinitionDetailsUrl(ReplicationDefinition replicationDefinition)
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
        // Is it currently running?
        List<ExecutionSummary> executing = 
              actionTrackingService.getExecutingActions(replicationDefinition);
        if(executing.size() == 0) {
           // It isn't running, we can use the persisted details
           model.put(DEFINITION_STATUS, replicationDefinition.getExecutionStatus().toString());
           model.put(DEFINITION_STARTED_AT, replicationDefinition.getExecutionStartDate());
           model.put(DEFINITION_ENDED_AT,   replicationDefinition.getExecutionEndDate());
           return;
        }
        
        // We have at least one copy running
        ExecutionSummary es;
        if(executing.size() == 1) {
           es = executing.get(0);
        } else {
           // More than one copy, joy
           // Go for the lowest execution instance id, so
           //  we're predictable
           es = executing.get(0);
           for(ExecutionSummary e : executing) {
              if(e.getExecutionInstance() < es.getExecutionInstance()) {
                 es = e;
              }
           }
        }
        
        // Update the details based on this
        ExecutionDetails details = actionTrackingService.getExecutionDetails(es);
        if(details.isCancelRequested()) {
           model.put(DEFINITION_STATUS, "CancelRequested");
        } else {
           model.put(DEFINITION_STATUS, "Running");
        }
        model.put(DEFINITION_STARTED_AT, details.getStartedAt());
        model.put(DEFINITION_ENDED_AT, null);
    }
}
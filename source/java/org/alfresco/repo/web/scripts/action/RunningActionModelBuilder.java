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
package org.alfresco.repo.web.scripts.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ISO8601DateFormat;

/**
 * Builds up models for running actions
 *  
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionModelBuilder 
{
    protected static final String MODEL_DATA_ITEM = "runningAction";
    protected static final String MODEL_DATA_LIST = "runningActions";
    
    protected static final String ACTION_ID = "id";
    protected static final String ACTION_TYPE = "type";
    protected static final String ACTION_INSTANCE = "instance";
    protected static final String ACTION_NODE_REF = "nodeRef";
    protected static final String ACTION_STARTED_AT = "startedAt";
    protected static final String ACTION_RUNNING_ON = "runningOn";
    protected static final String ACTION_CANCEL_REQUESTED = "cancelRequested";
    protected static final String ACTION_KEY = "key";
    
    
    protected NodeService nodeService;
    protected ActionService actionService;
    protected ActionTrackingService actionTrackingService;

    public RunningActionModelBuilder(NodeService nodeService, ActionService actionService,
                                   ActionTrackingService actionTrackingService) 
    {
       this.nodeService = nodeService;
       this.actionService = actionService;
       this.actionTrackingService = actionTrackingService;
    }
    
    
    /**
     * Build a model containing a single running action
     */
    protected Map<String,Object> buildSimpleModel(ExecutionSummary summary)
    {
       Map<String, Object> ram = buildModel(summary);
       if(ram != null) {
          Map<String, Object> model = new HashMap<String,Object>();
          model.put(MODEL_DATA_ITEM, ram);
          return model;
       }
       return null;
    }
    
    /**
     * Build a model containing a list of running actions for the given
     *  list of Running Actions
     */
    protected Map<String,Object> buildSimpleList(List<ExecutionSummary> runningActions)
    {
        List<Map<String,Object>> models = new ArrayList<Map<String,Object>>();
        
        for(ExecutionSummary summary : runningActions) {
           Map<String, Object> ram = buildModel(summary);
           if(ram != null) {
              models.add(ram);
           }
        }
        
        // Finish up
        Map<String, Object> model = new HashMap<String,Object>();
        model.put(MODEL_DATA_LIST, models);
        return model;
    }
    
    /**
     * Build a model for a single action
     */
    private Map<String,Object> buildModel(ExecutionSummary summary)
    {
        if(summary == null) {
           return null;
        }
        
        // Get the details, if we can
        ExecutionDetails details = actionTrackingService.getExecutionDetails(summary);
        
        // Only record if still running - may have finished
        //  between getting the list and now
        if(details != null) {
           Map<String, Object> ram = new HashMap<String,Object>();
           ram.put(ACTION_ID, summary.getActionId());
           ram.put(ACTION_TYPE, summary.getActionType());
           ram.put(ACTION_INSTANCE, summary.getExecutionInstance());
           ram.put(ACTION_KEY, AbstractActionWebscript.getRunningId(summary));
           
           ram.put(ACTION_NODE_REF, details.getPersistedActionRef());
           ram.put(ACTION_RUNNING_ON, details.getRunningOn());
           ram.put(ACTION_CANCEL_REQUESTED, details.isCancelRequested());
           
           if(details.getStartedAt() != null) {
              ram.put(ACTION_STARTED_AT, ISO8601DateFormat.format(details.getStartedAt()));
           } else {
              ram.put(ACTION_STARTED_AT, null);
           }
           
           return ram;
        }
        
        return null;
    }
}
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
package org.alfresco.repo.web.scripts.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 * @since 3.4
 *
 */
public class WorkflowInstancesGet extends AbstractWorkflowWebscript
{

    public static final String PARAM_STATE = "state";
    public static final String PARAM_INITIATOR = "initiator";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_DEFINITION_ID = "workflow_definition_id";

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        
        // get request parameters
        Map<String, String> filters = new HashMap<String, String>(4);
        filters.put(PARAM_STATE, req.getParameter(PARAM_STATE));
        filters.put(PARAM_INITIATOR, req.getParameter(PARAM_INITIATOR));
        filters.put(PARAM_DATE, req.getParameter(PARAM_DATE));
        filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
        
        String workflowDefinitionId = params.get(PARAM_DEFINITION_ID);
        
        List<WorkflowInstance> workflows = new ArrayList<WorkflowInstance>();
        
        if (workflowDefinitionId != null)
        {
            // list workflows for specified workflow definition
            workflows.addAll(workflowService.getWorkflows(workflowDefinitionId));
        }
        else
        {
            List<WorkflowDefinition> workflowDefinitions = workflowService.getAllDefinitions();
            
            // list workflows for all definitions
            for (WorkflowDefinition workflowDefinition : workflowDefinitions)
            {
                workflows.addAll(workflowService.getWorkflows(workflowDefinition.getId()));
            }
        }
        
        // filter result
        List<Map<String, Object>> results = new ArrayList<Map<String,Object>>(workflows.size());
        
        for (WorkflowInstance workflow : workflows)
        {
            if (matches(workflow, filters, modelBuilder))
            {
                results.add(modelBuilder.buildSimple(workflow));
            }
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        // build the model for ftl
        model.put("workflowInstances", results);

        return model;
    }
    
    /*
     * If workflow instance matches at list one filter value or if no filter was specified, then it will included in response
     */
    private boolean matches(WorkflowInstance workflowInstance, Map<String, String> filters, WorkflowModelBuilder modelBuilder)
    {
        // by default we assume that workflow instance should be included to response
        boolean result = true;
        boolean firstFilter = true;

        for (String key : filters.keySet())
        {
            String filterValue = filters.get(key);

            // skip null filters (null value means that filter was not specified)
            if (filterValue != null)
            {
                // some of the filter was specified, so the decision to include or not workflow to response
                // based on matching to filter parameter (by default false)
                if (firstFilter)
                {
                    result = false;
                    firstFilter = false;
                }

                boolean matches = false;

                if (key.equals(PARAM_STATE))
                {
                    WorkflowState filter = WorkflowState.getState(filterValue);

                    if (filter != null)
                    {
                        if (filter.equals(WorkflowState.COMPLETED) && !workflowInstance.isActive() || filter.equals(WorkflowState.ACTIVE) && workflowInstance.isActive())
                        {
                            matches = true;
                        }
                    }
                }
                else if (key.equals(PARAM_DATE))
                {
                    // only workflows that was started after specified time are returned
                    if (workflowInstance.getStartDate().getTime() >= ISO8601DateFormat.parse(filterValue.replaceAll(" ", "+")).getTime())
                    {
                        matches = true;
                    }
                }
                else if (key.equals(PARAM_INITIATOR))
                {
                    if (workflowInstance.getInitiator() != null && nodeService.exists(workflowInstance.getInitiator()) && filterValue.equals(nodeService.getProperty(workflowInstance.getInitiator(), ContentModel.PROP_USERNAME)))
                    {
                        matches = true;
                    }
                }
                else if (key.equals(PARAM_PRIORITY))
                {
                    WorkflowTask startTask = modelBuilder.getStartTaskForWorkflow(workflowInstance);

                    if (startTask != null && filterValue.equals(startTask.getProperties().get(WorkflowModel.PROP_WORKFLOW_PRIORITY).toString()))
                    {
                        matches = true;
                    }
                }
                // update global result
                result = result || matches;
            }
        }
        
        return result;
    }

    private enum WorkflowState
    {        
        ACTIVE ("active"),
        COMPLETED ("completed");
        
        String value;
        
        WorkflowState(String value)
        {
            this.value = value;
        }
        
        static WorkflowState getState(String value)
        {
            for (WorkflowState state : WorkflowState.values())
            {
                if (state.value.equals(value))
                {
                    return state;
                }
            }
            
            return null;
        }
    }
}
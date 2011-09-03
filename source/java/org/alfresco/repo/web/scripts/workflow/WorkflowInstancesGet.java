/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Java backed implementation for REST API to retrieve workflow instances.
 * 
 * @author Gavin Cornwell
 * @since 3.4
 */
public class WorkflowInstancesGet extends AbstractWorkflowWebscript
{
    public static final String PARAM_STATE = "state";
    public static final String PARAM_INITIATOR = "initiator";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_DUE_BEFORE = "dueBefore";
    public static final String PARAM_DUE_AFTER = "dueAfter";
    public static final String PARAM_STARTED_BEFORE = "startedBefore";
    public static final String PARAM_STARTED_AFTER = "startedAfter";
    public static final String PARAM_COMPLETED_BEFORE = "completedBefore";
    public static final String PARAM_COMPLETED_AFTER = "completedAfter";
    public static final String PARAM_DEFINITION_NAME = "definitionName";
    public static final String PARAM_DEFINITION_ID = "definitionId";
    public static final String VAR_DEFINITION_ID = "workflow_definition_id";
    
    private WorkflowInstanceDueAscComparator workflowComparator = new WorkflowInstanceDueAscComparator();

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // state is not included into filters list as it will be taken into account before filtering
        WorkflowState state = getState(req);
        
        // get filter param values
        Map<String, Object> filters = new HashMap<String, Object>(9);
        filters.put(PARAM_INITIATOR, req.getParameter(PARAM_INITIATOR));
        filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
        filters.put(PARAM_DEFINITION_NAME, req.getParameter(PARAM_DEFINITION_NAME));
        
        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0)
        {
            filters.put(PARAM_EXCLUDE, new ExcludeFilter(excludeParam));
        }
        
        // process all the date related parameters
        processDateFilter(req, PARAM_DUE_BEFORE, filters);
        processDateFilter(req, PARAM_DUE_AFTER, filters);
        processDateFilter(req, PARAM_STARTED_BEFORE, filters);
        processDateFilter(req, PARAM_STARTED_AFTER, filters);
        processDateFilter(req, PARAM_COMPLETED_BEFORE, filters);
        processDateFilter(req, PARAM_COMPLETED_AFTER, filters);
        
        // determine if there is a definition id to filter by
        String workflowDefinitionId = params.get(VAR_DEFINITION_ID);
        if (workflowDefinitionId == null)
        {
            workflowDefinitionId = req.getParameter(PARAM_DEFINITION_ID);
        }
        
        // default workflow state to ACTIVE if not supplied
        if (state == null)
        {
            state = WorkflowState.ACTIVE;
        }

        List<WorkflowInstance> workflows = new ArrayList<WorkflowInstance>();

        if (workflowDefinitionId != null)
        {
            // list workflows for specified workflow definition
            if (state == WorkflowState.ACTIVE)
            {
                workflows.addAll(workflowService.getActiveWorkflows(workflowDefinitionId));
            }
            else
            {
                workflows.addAll(workflowService.getCompletedWorkflows(workflowDefinitionId));
            }
        }
        else
        {
            List<WorkflowDefinition> workflowDefinitions = workflowService.getAllDefinitions();

            // list workflows for all definitions
            for (WorkflowDefinition workflowDefinition : workflowDefinitions)
            {
                if (state == WorkflowState.ACTIVE)
                {
                    workflows.addAll(workflowService.getActiveWorkflows(workflowDefinition.getId()));
                }
                else
                {
                    workflows.addAll(workflowService.getCompletedWorkflows(workflowDefinition.getId()));
                }
            }
        }
        
        // sort workflows by due date
        Collections.sort(workflows, workflowComparator);

        // filter result
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(workflows.size());

        for (WorkflowInstance workflow : workflows)
        {
            if (matches(workflow, filters, modelBuilder))
            {
                results.add(modelBuilder.buildSimple(workflow));
            }
        }

        // create and return results, paginated if necessary
        return createResultModel(req, "workflowInstances", results);
    }

    /**
     * Determine if the given workflow instance should be included in the response.
     * 
     * @param workflowInstance The workflow instance to check
     * @param filters The list of filters the task must match to be included
     * @return true if the workflow matches and should therefore be returned
     */
    private boolean matches(WorkflowInstance workflowInstance, Map<String, Object> filters, WorkflowModelBuilder modelBuilder)
    {
        // by default we assume that workflow instance should be included
        boolean result = true;

        for (String key : filters.keySet())
        {
            Object filterValue = filters.get(key);

            // skip null filters (null value means that filter was not specified)
            if (filterValue != null)
            {
                if (key.equals(PARAM_EXCLUDE))
                {
                    ExcludeFilter excludeFilter = (ExcludeFilter)filterValue;
                    String type = workflowInstance.getDefinition().getName();
                    
                    if (excludeFilter.isMatch(type))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_INITIATOR))
                {
                    NodeRef initiator = workflowInstance.getInitiator();
                    
                    if (initiator == null)
                    {
                        result = false;
                        break;
                    }
                    else
                    {
                        if (!nodeService.exists(initiator) || 
                            !filterValue.equals(nodeService.getProperty(workflowInstance.getInitiator(), ContentModel.PROP_USERNAME)))
                        {
                            result = false;
                            break;
                        }
                    }
                }
                else if (key.equals(PARAM_PRIORITY))
                {
                    String priority = "0";
                    if (workflowInstance.getPriority() != null)
                    {
                        priority = workflowInstance.getPriority().toString();
                    }

                    if (!filterValue.equals(priority))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_DEFINITION_NAME))
                {
                    String definitionName = workflowInstance.getDefinition().getName();
                    
                    if (!filterValue.equals(definitionName))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_DUE_BEFORE))
                {
                    Date dueDate = workflowInstance.getDueDate();

                    if (!isDateMatchForFilter(dueDate, filterValue, true))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_DUE_AFTER))
                {
                    Date dueDate = workflowInstance.getDueDate();

                    if (!isDateMatchForFilter(dueDate, filterValue, false))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_STARTED_BEFORE))
                {
                    Date startDate = workflowInstance.getStartDate();

                    if (!isDateMatchForFilter(startDate, filterValue, true))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_STARTED_AFTER))
                {
                    Date startDate = workflowInstance.getStartDate();

                    if (!isDateMatchForFilter(startDate, filterValue, false))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_COMPLETED_BEFORE))
                {
                    Date endDate = workflowInstance.getEndDate();

                    if (!isDateMatchForFilter(endDate, filterValue, true))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_COMPLETED_AFTER))
                {
                    Date endDate = workflowInstance.getEndDate();

                    if (!isDateMatchForFilter(endDate, filterValue, false))
                    {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }
    
    /**
     * Gets the specified {@link WorkflowState}, null if not requested.
     * 
     * @param req The WebScript request
     * @return The workflow state or null if not requested
     */
    private WorkflowState getState(WebScriptRequest req)
    {
        String stateName = req.getParameter(PARAM_STATE);
        if (stateName != null)
        {
            try
            {
                return WorkflowState.valueOf(stateName.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                String msg = "Unrecognised State parameter: " + stateName;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }
        
        return null;
    }

    // enum to represent workflow states
    private enum WorkflowState
    {
        ACTIVE,
        COMPLETED;
    }
    
    /**
     * Comparator to sort workflow instances by due date in ascending order.
     */
    class WorkflowInstanceDueAscComparator implements Comparator<WorkflowInstance>
    {
        @Override
        public int compare(WorkflowInstance o1, WorkflowInstance o2)
        {
            Date date1 = o1.getDueDate();
            Date date2 = o2.getDueDate();
            
            long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
            long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();
            
            long result = time1 - time2;
            
            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }
        
    }
}
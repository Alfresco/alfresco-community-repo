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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery.DatePosition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

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
    
    public static final QName QNAME_INITIATOR = QName.createQName(NamespaceService.DEFAULT_URI, "initiator");

    private WorkflowInstanceDueAscComparator workflowComparator = new WorkflowInstanceDueAscComparator();

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery();

        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // state is not included into filters list as it will be taken into account before filtering
        WorkflowState state = getState(req);
        
        // get filter param values
        Map<QName, Object> filters = new HashMap<QName, Object>(9);

        if (req.getParameter(PARAM_INITIATOR) != null)
            filters.put(QNAME_INITIATOR, personService.getPerson(req.getParameter(PARAM_INITIATOR)));

        if (req.getParameter(PARAM_PRIORITY) != null)
            filters.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, req.getParameter(PARAM_PRIORITY));
        
        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0)
        {
            workflowInstanceQuery.setExcludedDefinitions(Arrays.asList(StringUtils.tokenizeToStringArray(excludeParam, ",")));
        }
        
        // process all the date related parameters
        Map<DatePosition, Date> dateParams = new HashMap<DatePosition, Date>();
        Date dueBefore = getDateFromRequest(req, PARAM_DUE_BEFORE);
        if (dueBefore != null)
        {
            dateParams.put(DatePosition.BEFORE, dueBefore);
        }
        Date dueAfter = getDateFromRequest(req, PARAM_DUE_AFTER);
        if (dueAfter != null)
        {
            dateParams.put(DatePosition.AFTER, dueAfter);
        }
        if (dateParams.isEmpty())
        {
            if (req.getParameter(PARAM_DUE_BEFORE) != null || req.getParameter(PARAM_DUE_AFTER) != null)
            {
                filters.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, null);
            }
        }
        else
        {
            filters.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dateParams);
        }
                    
        workflowInstanceQuery.setStartBefore(getDateFromRequest(req, PARAM_STARTED_BEFORE));
        workflowInstanceQuery.setStartAfter(getDateFromRequest(req, PARAM_STARTED_AFTER));
        workflowInstanceQuery.setEndBefore(getDateFromRequest(req, PARAM_COMPLETED_BEFORE));
        workflowInstanceQuery.setEndAfter(getDateFromRequest(req, PARAM_COMPLETED_AFTER));

        // determine if there is a definition id to filter by
        String workflowDefinitionId = params.get(VAR_DEFINITION_ID);
        if (workflowDefinitionId == null)
        {
            workflowDefinitionId = req.getParameter(PARAM_DEFINITION_ID);
            if (workflowDefinitionId == null)
            {
                if (req.getParameter(PARAM_DEFINITION_NAME) != null)
                {
                    workflowDefinitionId = workflowService.getDefinitionByName(req.getParameter(PARAM_DEFINITION_NAME)).getId();
                }
            }
        }
                    
        // default workflow state to ACTIVE if not supplied
        if (state == null)
        {
            state = WorkflowState.ACTIVE;
        }

        workflowInstanceQuery.setActive(state == WorkflowState.ACTIVE);
        workflowInstanceQuery.setCustomProps(filters);

        List<WorkflowInstance> workflows = new ArrayList<WorkflowInstance>();

        if (workflowDefinitionId != null)
        {
            workflowInstanceQuery.setWorkflowDefinitionId(workflowDefinitionId);
        }

        // MNT-9074 My Tasks fails to render if tasks quantity is excessive
        int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);

        workflows.addAll(workflowService.getWorkflows(workflowInstanceQuery, maxItems, skipCount));
        
        int total = (int) workflowService.countWorkflows(workflowInstanceQuery);
        
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(total);
        
        // init empty list 
        results.addAll(Arrays.asList((Map<String, Object>[]) new Map[total]));

        for (WorkflowInstance workflow : workflows)
        {
            // set to special index
            results.set(skipCount, modelBuilder.buildSimple(workflow));
            
            skipCount++;
        }

        // create and return results, paginated if necessary
        return createResultModel(req, "workflowInstances", results);
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
        ACTIVE, COMPLETED;
    }

    private Date getDateFromRequest(WebScriptRequest req, String paramName)
    {
        String dateParam = req.getParameter(paramName);
        if (dateParam != null)
        {
            if (!EMPTY.equals(dateParam) && !NULL.equals(dateParam))
            {
                return getDateParameter(req, paramName);
            }
        }

        return null;
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
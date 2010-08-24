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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 *
 */
public class TaskInstancesGet extends AbstractWorkflowWebscript
{
    public static final String PARAM_AUTHORITY = "authority";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_DUE_BEFORE = "dueBefore";
    public static final String PARAM_DUE_AFTER = "dueAfter";
    public static final String PARAM_PROPERTIES = "properties";
    public static final String PARAM_DETAILED = "detailed";

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> filters = new HashMap<String, Object>(4);

        // authority is not included into filters list as it will be taken into account before filtering
        String authority = getAuthority(req);
        // state is also not included into filters list, for the same reason
        WorkflowTaskState state = getState(req);
        filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
        filters.put(PARAM_DUE_BEFORE, getDateParameter(req, PARAM_DUE_BEFORE));
        filters.put(PARAM_DUE_AFTER, getDateParameter(req, PARAM_DUE_AFTER));

        List<String> properties = getProperties(req);
        boolean detailed = "true".equals(req.getParameter(PARAM_DETAILED));

        List<WorkflowTask> allTasks;

        if (authority != null)
        {
            List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state);
            List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(authority);
            allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
            allTasks.addAll(tasks);
            allTasks.addAll(pooledTasks);
        }
        else
        {
            // authority was not provided -> return all active tasks in the system
            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setTaskState(state);
            taskQuery.setActive(null);
            allTasks = workflowService.queryTasks(taskQuery);
        }

        // filter results
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (WorkflowTask task : allTasks)
        {
            if (matches(task, filters))
            {
                if (detailed)
                {
                    results.add(modelBuilder.buildDetailed(task));
                }
                else
                {
                    results.add(modelBuilder.buildSimple(task, properties));
                }
            }
        }

        // create and return results, paginated if necessary
        return createResultModel(modelBuilder, req, "taskInstances", results);
    }

    private List<String> getProperties(WebScriptRequest req)
    {
        String propertiesStr = req.getParameter(PARAM_PROPERTIES);
        if (propertiesStr != null)
        {
            return Arrays.asList(propertiesStr.split(","));
        }
        return null;
    }

    /**
     * Gets the specified {@link WorkflowTaskState}, defaults to IN_PROGRESS.
     * @param req
     * @return
     */
    private WorkflowTaskState getState(WebScriptRequest req)
    {
        String stateName = req.getParameter(PARAM_STATE);
        if (stateName != null)
        {
            try
            {
                return WorkflowTaskState.valueOf(stateName.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                String msg = "Unrecognised State parameter:  " + stateName;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }
        // Defaults to IN_PROGRESS.
        return WorkflowTaskState.IN_PROGRESS;
    }

    /**
     * Returns the specified authority. If no authority is specified then returns the current Fully Authenticated user.
     * @param req
     * @return
     */
    private String getAuthority(WebScriptRequest req)
    {
        String authority = req.getParameter(PARAM_AUTHORITY);
        if (authority == null || authority.length() == 0)
        {
            authority = null;
        }
        return authority;
    }

    /*
     * If workflow task matches at list one filter value or if no filter was specified, then it will be included in response
     */
    private boolean matches(WorkflowTask task, Map<String, Object> filters)
    {
        // by default we assume that workflow task should be included to response
        boolean result = true;
        boolean firstFilter = true;

        for (String key : filters.keySet())
        {
            Object filterValue = filters.get(key);

            // skip null filters (null value means that filter was not specified)
            if (filterValue != null)
            {
                // some of the filter was specified, so the decision to include or not task to response 
                // based on matching to filter parameter (by default false)
                if (firstFilter)
                {
                    result = false;
                    firstFilter = false;
                }

                boolean matches = false;

                if (key.equals(PARAM_DUE_BEFORE))
                {
                    Serializable dueDate = task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (dueDate == null || ((Date) dueDate).getTime() <= ((Date) filterValue).getTime())
                    {
                        matches = true;
                    }
                }
                else if (key.equals(PARAM_DUE_AFTER))
                {
                    Serializable dueDate = task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (dueDate == null || ((Date) dueDate).getTime() >= ((Date) filterValue).getTime())
                    {
                        matches = true;
                    }
                }
                else if (key.equals(PARAM_PRIORITY))
                {
                    if (filterValue.equals(task.getProperties().get(WorkflowModel.PROP_PRIORITY).toString()))
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

}

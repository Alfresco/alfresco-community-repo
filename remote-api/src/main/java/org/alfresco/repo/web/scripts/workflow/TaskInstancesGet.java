/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;

/**
 * Webscript impelementation to return workflow task instances.
 * 
 * @author Nick Smith
 * @author Gavin Cornwell
 * @since 3.4
 */
public class TaskInstancesGet extends AbstractWorkflowWebscript
{
    private static final Log LOGGER = LogFactory.getLog(TaskInstancesGet.class);

    public static final String PARAM_AUTHORITY = "authority";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_DUE_BEFORE = "dueBefore";
    public static final String PARAM_DUE_AFTER = "dueAfter";
    public static final String PARAM_PROPERTIES = "properties";
    public static final String PARAM_POOLED_TASKS = "pooledTasks";
    public static final String PARAM_PROPERTY = "property";
    public static final String VAR_WORKFLOW_INSTANCE_ID = "workflow_instance_id";

    private WorkflowTaskDueAscComparator taskComparator = new WorkflowTaskDueAscComparator();

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        Map<String, Object> filters = new HashMap<String, Object>(4);

        // authority is not included into filters list as it will be taken into account before filtering
        String authority = getAuthority(req);

        if (authority == null)
        {
            // ALF-11036 fix, if authority argument is omitted the tasks for the current user should be returned.
            authority = authenticationService.getCurrentUserName();
        }

        // state is also not included into filters list, for the same reason
        WorkflowTaskState state = getState(req);

        // look for a workflow instance id
        String workflowInstanceId = params.get(VAR_WORKFLOW_INSTANCE_ID);

        // determine if pooledTasks should be included, when appropriate i.e. when an authority is supplied
        Boolean pooledTasksOnly = getPooledTasks(req);

        // get list of properties to include in the response
        List<String> properties = getProperties(req);

        // get filter param values
        filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
        filters.put(PARAM_PROPERTY, req.getParameter(PARAM_PROPERTY));
        processDateFilter(req, PARAM_DUE_BEFORE, filters);
        processDateFilter(req, PARAM_DUE_AFTER, filters);

        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0)
        {
            filters.put(PARAM_EXCLUDE, new ExcludeFilter(excludeParam));
        }

        List<WorkflowTask> allTasks;

        if (workflowInstanceId != null)
        {
            // a workflow instance id was provided so query for tasks
            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setActive(null);
            taskQuery.setProcessId(workflowInstanceId);
            taskQuery.setTaskState(state);
            taskQuery.setOrderBy(new OrderBy[]{OrderBy.TaskDue_Asc});

            if (authority != null)
            {
                taskQuery.setActorId(authority);
            }

            allTasks = workflowService.queryTasks(taskQuery);
        }
        else
        {
            // default task state to IN_PROGRESS if not supplied
            if (state == null)
            {
                state = WorkflowTaskState.IN_PROGRESS;
            }

            // no workflow instance id is present so get all tasks
            if (authority != null)
            {
                List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state, true);
                List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(authority, true);
                if (pooledTasksOnly != null)
                {
                    if (pooledTasksOnly.booleanValue())
                    {
                        // only return pooled tasks the user can claim
                        allTasks = new ArrayList<WorkflowTask>(pooledTasks.size());
                        allTasks.addAll(pooledTasks);
                    }
                    else
                    {
                        // only return tasks assigned to the user
                        allTasks = new ArrayList<WorkflowTask>(tasks.size());
                        allTasks.addAll(tasks);
                    }
                }
                else
                {
                    // include both assigned and unassigned tasks
                    allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
                    allTasks.addAll(tasks);
                    allTasks.addAll(pooledTasks);
                }

                // sort tasks by due date
                Collections.sort(allTasks, taskComparator);
            }
            else
            {
                // authority was not provided -> return all active tasks in the system
                WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
                taskQuery.setTaskState(state);
                taskQuery.setActive(null);
                taskQuery.setOrderBy(new OrderBy[]{OrderBy.TaskDue_Asc});
                allTasks = workflowService.queryTasks(taskQuery);
            }
        }

        int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
        int totalCount = 0;
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        // Filter results
        for (WorkflowTask task : allTasks)
        {
            if (matches(task, filters))
            {
                // Total-count needs to be based on matching tasks only, so we can't just use allTasks.size() for this
                totalCount++;
                if (totalCount > skipCount && (maxItems < 0 || maxItems > results.size()))
                {
                    // Only build the actual detail if it's in the range of items we need. This will
                    // drastically improve performance over paging after building the model
                    results.add(modelBuilder.buildSimple(task, properties));
                }
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("taskInstances", results);

        if (maxItems != DEFAULT_MAX_ITEMS || skipCount != DEFAULT_SKIP_COUNT)
        {
            // maxItems or skipCount parameter was provided so we need to include paging into response
            model.put("paging", ModelUtil.buildPaging(totalCount, maxItems == DEFAULT_MAX_ITEMS ? totalCount : maxItems, skipCount));
        }

        // create and return results, paginated if necessary
        return model;
    }

    /**
     * Retrieves the list of property names to include in the response.
     * 
     * @param req
     *            The WebScript request
     * @return List of property names
     */
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
     * Retrieves the pooledTasks parameter.
     * 
     * @param req
     *            The WebScript request
     * @return null if not present, Boolean object otherwise
     */
    private Boolean getPooledTasks(WebScriptRequest req)
    {
        Boolean result = null;
        String includePooledTasks = req.getParameter(PARAM_POOLED_TASKS);

        if (includePooledTasks != null)
        {
            result = Boolean.valueOf(includePooledTasks);
        }

        return result;
    }

    /**
     * Gets the specified {@link WorkflowTaskState}, null if not requested
     * 
     * @param req
     *            WebScriptRequest
     * @return WorkflowTaskState
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
                String msg = "Unrecognised State parameter: " + stateName;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }

        return null;
    }

    /**
     * Returns the specified authority. If no authority is specified then returns the current Fully Authenticated user.
     * 
     * @param req
     *            WebScriptRequest
     * @return String
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

    /**
     * Determine if the given task should be included in the response.
     * 
     * @param task
     *            The task to check
     * @param filters
     *            The list of filters the task must match to be included
     * @return true if the task matches and should therefore be returned
     */
    private boolean matches(WorkflowTask task, Map<String, Object> filters)
    {
        // by default we assume that workflow task should be included
        boolean result = true;

        for (String key : filters.keySet())
        {
            Object filterValue = filters.get(key);

            // skip null filters (null value means that filter was not specified)
            if (filterValue != null)
            {
                if (key.equals(PARAM_EXCLUDE))
                {
                    ExcludeFilter excludeFilter = (ExcludeFilter) filterValue;
                    String type = task.getDefinition().getMetadata().getName().toPrefixString(this.namespaceService);
                    if (excludeFilter.isMatch(type))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_DUE_BEFORE))
                {
                    Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (!isDateMatchForFilter(dueDate, filterValue, true))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_DUE_AFTER))
                {
                    Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (!isDateMatchForFilter(dueDate, filterValue, false))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_PRIORITY))
                {
                    if (!filterValue.equals(task.getProperties().get(WorkflowModel.PROP_PRIORITY).toString()))
                    {
                        result = false;
                        break;
                    }
                }
                else if (key.equals(PARAM_PROPERTY))
                {
                    int propQNameEnd = filterValue.toString().indexOf('/');
                    if (propQNameEnd < 1)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Ignoring invalid property filter:" + filterValue.toString());
                        }
                        break;
                    }
                    String propValue = filterValue.toString().substring(propQNameEnd + 1);
                    if (propValue.isEmpty())
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Ignoring empty property value filter [" + propValue + "]");
                        }
                        break;
                    }
                    String propQNameStr = filterValue.toString().substring(0, propQNameEnd);
                    QName propertyQName;
                    try
                    {
                        propertyQName = QName.createQName(propQNameStr, namespaceService);
                    }
                    catch (Exception ex)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Ignoring invalid QName property filter [" + propQNameStr + "]");
                        }
                        break;
                    }
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Filtering with property [" + propertyQName.toPrefixString(namespaceService) + "=" + propValue + "]");
                    }
                    Serializable value = task.getProperties().get(propertyQName);
                    if (value != null && !value.equals(propValue))
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
     * Comparator to sort workflow tasks by due date in ascending order.
     */
    class WorkflowTaskDueAscComparator implements Comparator<WorkflowTask>
    {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2)
        {
            Date date1 = (Date) o1.getProperties().get(WorkflowModel.PROP_DUE_DATE);
            Date date2 = (Date) o2.getProperties().get(WorkflowModel.PROP_DUE_DATE);

            long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
            long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();

            long result = time1 - time2;

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }
}

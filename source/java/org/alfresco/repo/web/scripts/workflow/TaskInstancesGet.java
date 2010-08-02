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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.workflow.WorkflowTask;
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
    public static final String PARAM_STATUS= "status";
    public static final String PARAM_PROPERTIES= "properties";
    public static final String PARAM_DETAILED= "detailed";
    
    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status,
            Cache cache)
    {
        String authority = getAuthority(req);
        WorkflowTaskState state = getState(req);
        List<String> properties = getProperties(req);
        boolean detailed = "true".equals(req.getParameter(PARAM_DETAILED));

        //TODO Handle possible thrown exceptions here?
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state);
        List<WorkflowTask> pooledTasks= workflowService.getPooledTasks(authority);
        ArrayList<WorkflowTask> allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
        allTasks.addAll(tasks);
        allTasks.addAll(pooledTasks);
        
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (WorkflowTask task : allTasks) 
        {
            if (detailed)
            {
                results.add(modelBuilder.buildDetailed(task));
            }
            else {
                results.add(modelBuilder.buildSimple(task, properties));
            }
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("taskInstances", results);
        return model;
    }

    private List<String> getProperties(WebScriptRequest req)
    {
        String propertiesStr = req.getParameter(PARAM_PROPERTIES);
        if(propertiesStr != null)
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
        String stateName= req.getParameter(PARAM_STATUS);
        if(stateName != null)
        {
            try
            {
                return WorkflowTaskState.valueOf(stateName.toUpperCase());
            }
            catch(IllegalArgumentException e)
            {
                String msg = "Unrecognised State parameter:  "+stateName;
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
        if(authority == null)
        {
            authority = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        return authority;
    }

}

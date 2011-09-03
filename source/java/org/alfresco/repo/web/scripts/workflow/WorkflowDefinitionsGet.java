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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript impelementation to return the latest version of all deployed
 * workflow definitions.
 * 
 * @author Gavin Cornwell
 * @author Nick Smith
 * @since 3.4
 */
public class WorkflowDefinitionsGet extends AbstractWorkflowWebscript
{
    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        ExcludeFilter excludeFilter = null;
        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0)
        {
            excludeFilter = new ExcludeFilter(excludeParam);
        }
        
        // list all workflow's definitions simple representation
        List<WorkflowDefinition> workflowDefinitions = workflowService.getDefinitions();            
            
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        
        for (WorkflowDefinition workflowDefinition : workflowDefinitions)            
        {
            // if present, filter out excluded definitions
            if (excludeFilter == null || !excludeFilter.isMatch(workflowDefinition.getName()))
            {
                results.add(modelBuilder.buildSimple(workflowDefinition));
            }
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("workflowDefinitions", results);
        return model;
    }
}
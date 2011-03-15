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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript impelementation to return the latest version of a workflow
 * definition.
 * 
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class WorkflowDefinitionGet extends AbstractWorkflowWebscript
{
	private static final String PARAM_WORKFLOW_DEFINITION_ID = "workflowDefinitionId";
	
    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
    	Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // Get the definition id from the params
        String workflowDefinitionId = params.get(PARAM_WORKFLOW_DEFINITION_ID);
        
    	WorkflowDefinition workflowDefinition = workflowService.getDefinitionById(workflowDefinitionId);
    	
    	// Workflow definition is not found, 404
        if (workflowDefinition == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, 
            		"Unable to find workflow definition with id: " + workflowDefinitionId);
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("workflowDefinition", modelBuilder.buildDetailed(workflowDefinition));
        return model;
    }
}
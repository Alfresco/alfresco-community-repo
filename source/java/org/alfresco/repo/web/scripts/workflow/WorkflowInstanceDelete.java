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

import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 * @since 3.4
 *
 */
public class WorkflowInstanceDelete extends AbstractWorkflowWebscript
{

    public static final String PARAM_FORCED = "forced";
    
    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // getting workflow instance id from request parameters
        String workflowInstanceId = params.get("workflow_instance_id");

        boolean forced = getForced(req);
        
        if (forced)
        {
            workflowService.deleteWorkflow(workflowInstanceId);
        }
        else
        {
            workflowService.cancelWorkflow(workflowInstanceId);
        }
        
        return null;
    }
    
    private boolean getForced(WebScriptRequest req)
    {
        String forced = req.getParameter(PARAM_FORCED);
        if (forced != null)
        {
            try
            {
                return Boolean.valueOf(forced);
            }
            catch (Exception e)
            {
                // do nothing, false will be returned
            }
        }

        // Defaults to false.
        return false;
    }
}

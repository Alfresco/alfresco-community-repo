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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * Java backed implementation for REST API to retrieve a diagram of a workflow instance.
 * 
 * @author Gavin Cornwell
 * @since 4.0
 */
public class WorkflowInstanceDiagramGet extends AbstractWebScript
{
    protected WorkflowService workflowService;
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // getting workflow instance id from request parameters
        String workflowInstanceId = params.get("workflow_instance_id");
        
        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);

        // workflow instance was not found -> return 404
        if (workflowInstance == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find workflow instance with id: " + workflowInstanceId);
        }
        
        // check whether there is a diagram available
        if (!workflowService.hasWorkflowImage(workflowInstanceId))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find diagram for workflow instance with id: " + workflowInstanceId);
        }
        
        // set mimetype for the content and the character encoding + length for the stream
        res.setContentType(MimetypeMap.MIMETYPE_IMAGE_PNG);
        
        // set caching (never cache)
        Cache cache = new Cache();
        cache.setNeverCache(true);
        cache.setMustRevalidate(true);
        cache.setMaxAge(0L);
        res.setCache(cache);
        
        // stream image back to client
        InputStream imageData = workflowService.getWorkflowImage(workflowInstanceId);
        try
        {
            FileCopyUtils.copy(imageData, res.getOutputStream());     // both streams are closed
        }
        catch (IOException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred streaming diagram for workflow instance with id '" + 
                        workflowInstanceId + "' " + e.getMessage());
        }
    }
}

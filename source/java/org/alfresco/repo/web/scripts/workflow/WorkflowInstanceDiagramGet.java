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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.TempFileProvider;
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
public class WorkflowInstanceDiagramGet extends StreamContent
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
        
        // copy image data into temporary file
        File file = TempFileProvider.createTempFile("workflow-diagram-", ".png");
        InputStream imageData = workflowService.getWorkflowImage(workflowInstanceId);
        OutputStream os = new FileOutputStream(file);
        FileCopyUtils.copy(imageData, os);
        
        // stream temporary file back to client
        streamContent(req, res, file, false);
    }
}

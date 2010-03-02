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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/*
 * Render Workflow Definition Image
 */
public class WorkflowDefinitionImageServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException
    {
        // retrieve workflow definition id
        String uri = request.getRequestURI();
        uri = uri.substring(request.getContextPath().length());
        StringTokenizer t = new StringTokenizer(uri, "/");
        if (t.countTokens() != 2)
        {
            throw new WorkflowException("Workflow Definition Image servlet does not contain workflow definition id : " + uri); 
        }
        t.nextToken();  // skip servlet name
        String workflowDefinitionId = t.nextToken();

        // retrieve workflow definition image
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        WorkflowService workflowService = (WorkflowService)wc.getBean(ServiceRegistry.WORKFLOW_SERVICE.getLocalName());
        byte[] definitionImage = workflowService.getDefinitionImage(workflowDefinitionId); 
        
        // stream out through response
        OutputStream out = response.getOutputStream();
        out.write(definitionImage);
        out.flush();
	}
    
}

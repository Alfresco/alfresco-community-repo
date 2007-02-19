/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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

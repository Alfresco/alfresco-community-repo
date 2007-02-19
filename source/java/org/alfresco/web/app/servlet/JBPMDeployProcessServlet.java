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
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Servlet for handling process deployments from jBPM process designer.
 * 
 * @author davidc
 */
public class JBPMDeployProcessServlet extends HttpServlet
{
    private static final long serialVersionUID = 8002539291245090187L;


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        try
        {
            response.setContentType("text/html");
            InputStream deploymentArchive = getDeploymentArchive(request);
            WorkflowDefinition workflowDef = deployArchive(deploymentArchive);
            response.getWriter().println("Deployed archive " + workflowDef.title + " successfully");
        }
        catch(IOException e)
        {
            // NOTE: according to original jBPM deployment servlet
            response.getWriter().println("IOException");
        }
        catch(FileUploadException e)
        {
            // NOTE: according to original jBPM deployment servlet
            response.getWriter().println("FileUploadException");
        }
    }

    
    /**
     * Retrieve the JBPM Process Designer deployment archive from the request
     * 
     * @param request  the request
     * @return  the input stream onto the deployment archive
     * @throws WorkflowException
     * @throws FileUploadException
     * @throws IOException
     */
    private InputStream getDeploymentArchive(HttpServletRequest request)
        throws FileUploadException, IOException
    {
        if (!FileUpload.isMultipartContent(request))
        {
            throw new FileUploadException("Not a multipart request");
        }
        
        GPDUpload fileUpload = new GPDUpload();
        List list = fileUpload.parseRequest(request);
        Iterator iterator = list.iterator();
        if (!iterator.hasNext())
        {
            throw new FileUploadException("No process file in the request");
        }
     
        FileItem fileItem = (FileItem) iterator.next();
        if (fileItem.getContentType().indexOf("application/x-zip-compressed") == -1)
        {
            throw new FileUploadException("Not a process archive");
        }
        
        return fileItem.getInputStream();
    }
    
    
    /**
     * Deploy the jBPM process archive to the Alfresco Repository
     * 
     * @param deploymentArchive  the archive to deploy
     * @return  the deployed workflow definition
     */
    private WorkflowDefinition deployArchive(InputStream deploymentArchive)
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        WorkflowService workflowService = (WorkflowService)wc.getBean(ServiceRegistry.WORKFLOW_SERVICE.getLocalName());
        WorkflowDeployment deployment = workflowService.deployDefinition("jbpm", deploymentArchive, MimetypeMap.MIMETYPE_ZIP); 
        return deployment.definition; 
    }


    /**
     * NOTE: Workaround...
     * 
     * The JBPM process designer (as of 3.1.2) issues a request with a multi-part line
     * delimited by ",".  It should be ":" according to the HTTP specification which
     * the commons file-upload is adhering to.
     * 
     * @author davidc
     */
    @SuppressWarnings("deprecation")
    private class GPDUpload extends DiskFileUpload
    {
        @Override
        protected byte[] getBoundary(String contentType)
        {
            return super.getBoundary(contentType.replace(",", ";"));
        }
    }
    
}
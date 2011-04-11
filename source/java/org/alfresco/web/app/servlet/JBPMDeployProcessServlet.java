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
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.PropertyCheck;
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
    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String PROP_ENABLED = "system.workflow.deployservlet.enabled";

    @Override
    public void init() throws ServletException
    {
        // Render this servlet permanently unavailable if its enablement property is not set
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        Properties globalProperties = (Properties) wc.getBean(BEAN_GLOBAL_PROPERTIES);
        String enabled = globalProperties.getProperty(PROP_ENABLED);
        if (!PropertyCheck.isValidPropertyString(enabled) || !Boolean.parseBoolean(enabled))
        {
            throw new UnavailableException("system.workflow.deployservlet.enabled=false");
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
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
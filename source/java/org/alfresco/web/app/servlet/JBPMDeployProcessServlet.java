/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
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
        // NOTE: retrieve jbpm engine directly as this servlet only serves JBPM process designer deployments
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        WorkflowComponent jbpmEngine = (WorkflowComponent)wc.getBean("jbpm_engine");
        return jbpmEngine.deployDefinition(deploymentArchive, MimetypeMap.MIMETYPE_ZIP);
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
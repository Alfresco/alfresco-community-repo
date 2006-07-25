/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.webapp.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

    
    private class GPDUpload extends DiskFileUpload
    {

        @Override
        protected byte[] getBoundary(String contentType)
        {
            return super.getBoundary(contentType.replace(",", ";"));
        }
    }
    
    
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.getWriter().println(handleRequest(request));
	}
	
	public void printInput(HttpServletRequest request) throws IOException {
		InputStream inputStream = request.getInputStream();
		StringBuffer buffer = new StringBuffer();
		int read;
		while ((read = inputStream.read()) != -1) {
			buffer.append((char)read);
		}
		log.debug(buffer.toString());
	}
	
	private String handleRequest(HttpServletRequest request) {
	    if (!FileUpload.isMultipartContent(request)) {
			log.debug("Not a multipart request");
	    	return "Not a multipart request";
	    }
		try {
		    GPDUpload fileUpload = new GPDUpload();
		    List list = fileUpload.parseRequest(request);
		    Iterator iterator = list.iterator();
		    if (!iterator.hasNext()) {
				log.debug("No process file in the request");
		    	return "No process file in the request";
		    }
		    FileItem fileItem = (FileItem)iterator.next();
		    if (fileItem.getContentType().indexOf("application/x-zip-compressed") == -1) {
				log.debug("Not a process archive");
		    	return "Not a process archive";
		    }
		    return doDeployment(fileItem);
		} catch (FileUploadException e) {
			e.printStackTrace();
			return "FileUploadException";
		}		
	}
	
	private String doDeployment(FileItem fileItem) {
		try {
		   ZipInputStream zipInputStream = new ZipInputStream(fileItem.getInputStream());
		   JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
		   ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zipInputStream);	
		   log.debug("Created a processdefinition : " + processDefinition.getName() );
		   jbpmContext.deployProcessDefinition(processDefinition);
		   zipInputStream.close();
		   return "Deployed archive " + processDefinition.getName() + " successfully";
		} catch (IOException e) {
			return "IOException";
		}
	}
	
	  private static Log log = LogFactory.getLog(UploadServlet.class);

}
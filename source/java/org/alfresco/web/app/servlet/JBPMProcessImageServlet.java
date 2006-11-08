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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;


//
//
// TODO: DC: Tidy up
//
//




public class JBPMProcessImageServlet extends HttpServlet {
  
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
    long processDefinitionId = Long.parseLong( request.getParameter( "definitionId" ) );
    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    ProcessDefinition processDefinition = jbpmContext.getGraphSession().loadProcessDefinition(processDefinitionId);
    byte[] bytes = processDefinition.getFileDefinition().getBytes("processimage.jpg");
    OutputStream out = response.getOutputStream();
    out.write(bytes);
    out.flush();
    
    // leave this in.  it is in case we want to set the mime type later.
    // get the mime type
    // String contentType = URLConnection.getFileNameMap().getContentTypeFor( fileName );
    // set the content type (=mime type)
    // response.setContentType( contentType );
	}
}

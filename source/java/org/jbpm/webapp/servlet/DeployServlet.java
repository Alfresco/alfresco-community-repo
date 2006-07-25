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
import java.io.PrintWriter;
import java.net.URL;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

/**
 * servlet to be used by the process designer to deploy processes. 
 */
public class DeployServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String archive = request.getParameter("archive");
    log.debug("deploying archive "+archive);

    PrintWriter writer = response.getWriter();
    try {
      URL archiveUrl = new URL(archive);
      ZipInputStream zis = new ZipInputStream(archiveUrl.openStream());
      JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
      ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zis);
      jbpmContext.deployProcessDefinition(processDefinition);
      zis.close();
      
      writer.write("Deployed archive "+archive+" successfully");
      
    } catch (Exception e) {
      e.printStackTrace();
      writer.write("Deploying archive "+archive+" failed");
    }
  }

  private static Log log = LogFactory.getLog(DeployServlet.class);
}

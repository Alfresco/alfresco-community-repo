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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.template.Workflow;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.web.app.servlet.BaseTemplateContentServlet;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean used by an AJAX control to send information back on the requested workflow task.
 * 
 * @author Kevin Roast
 */
public class TaskInfoBean
{
   private WorkflowService workflowService;
   
   /**
    * Returns information for the workflow task identified by the 'taskId'
    * parameter found in the ExternalContext.
    * <p>
    * The result is the formatted HTML to show on the client.
    */
   public void sendTaskInfo() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      String taskId = (String)context.getExternalContext().getRequestParameterMap().get("taskId");
      if (taskId == null || taskId.length() == 0)
      {
         throw new IllegalArgumentException("'taskId' parameter is missing");
      }
      
      WorkflowTask task = this.workflowService.getTaskById(taskId);
      if (task != null)
      {
         Repository.getServiceRegistry(context).getTemplateService().processTemplate(
               "/alfresco/templates/client/task_summary_panel.ftl", getModel(task), out);
      }
      else
      {
         out.write("<span class='errorMessage'>Task could not be found.</span>");
      }
   }
   
   /**
    * Returns the resource list for the workflow task identified by the 'taskId'
    * parameter found in the ExternalContext.
    * <p>
    * The result is the formatted HTML to show on the client.
    */
   public void sendTaskResources() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      String taskId = (String)context.getExternalContext().getRequestParameterMap().get("taskId");
      if (taskId == null || taskId.length() == 0)
      {
         throw new IllegalArgumentException("'taskId' parameter is missing");
      }
      
      WorkflowTask task = this.workflowService.getTaskById(taskId);
      if (task != null)
      {
         Repository.getServiceRegistry(context).getTemplateService().processTemplate(
               "/alfresco/templates/client/task_resource_panel.ftl", getModel(task), out);
      }
      else
      {
         out.write("<span class='errorMessage'>Task could not be found.</span>");
      }
   }

   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param workflowService    The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   private Map<String, Object> getModel(WorkflowTask task)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      // create template api methods and objects
      model.put("date", new Date());
      model.put("url", new BaseTemplateContentServlet.URLHelper(
              context.getExternalContext().getRequestContextPath()));
      model.put("task", new Workflow.WorkflowTaskItem(
            Repository.getServiceRegistry(context),
            this.imageResolver,
            task));
      
      return model;
   }
   
   /** Template Image resolver helper */
   private TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, FileTypeImageSize size)
      {
         return Utils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
}

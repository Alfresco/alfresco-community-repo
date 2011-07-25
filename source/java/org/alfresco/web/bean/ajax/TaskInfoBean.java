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
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.Workflow;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.web.app.servlet.BaseTemplateContentServlet;
import org.alfresco.web.bean.repository.Repository;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Bean used by an AJAX control to send information back on the requested workflow task.
 * 
 * @author Kevin Roast
 */
public class TaskInfoBean implements Serializable
{
   private static final long serialVersionUID = -6627537519541525897L;
   
   transient private WorkflowService workflowService;
   
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
      
      WorkflowTask task = this.getWorkflowService().getTaskById(taskId);
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
      
      WorkflowTask task = this.getWorkflowService().getTaskById(taskId);
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
   
   private WorkflowService getWorkflowService()
   {
      if (workflowService == null)
      {
         workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
      return workflowService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   private Map<String, Object> getModel(WorkflowTask task)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      I18NUtil.registerResourceBundle("alfresco.messages.webclient");
      // create template api methods and objects
      model.put("date", new Date());
      model.put("msg", new I18NMessageMethod());
      model.put("url", new BaseTemplateContentServlet.URLHelper(context));
      model.put("locale", I18NUtil.getLocale());
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
         return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
}

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
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.workflow.WorkflowBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Command to execute the Manage Task dialog via url.
 * <p>
 * Arguments: id = the id of the task
 *            type = the qname type of the task
 * 
 * @author Kevin Roast
 */
public class ManageTaskDialogCommand extends BaseUIActionCommand
{
   public static final String PROP_TASKID   = "id";
   public static final String PROP_TASKTYPE = "type";
   
   private static final String[] PROPERTIES = new String[] {
      PROP_SERVLETCONTEXT, PROP_REQUEST, PROP_RESPONSE, PROP_TASKID, PROP_TASKTYPE};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      ServletContext sc = (ServletContext)properties.get(PROP_SERVLETCONTEXT);
      ServletRequest req = (ServletRequest)properties.get(PROP_REQUEST);
      ServletResponse res = (ServletResponse)properties.get(PROP_RESPONSE);
      FacesContext fc = FacesHelper.getFacesContext(req, res, sc, "/jsp/close.jsp");
      WorkflowBean wfBean = (WorkflowBean)FacesHelper.getManagedBean(fc, WorkflowBean.BEAN_NAME);
      
      // setup dialog context from url args in properties map
      String taskId = (String)properties.get(PROP_TASKID);
      ParameterCheck.mandatoryString(PROP_TASKID, taskId);
      String taskType = (String)properties.get(PROP_TASKTYPE);
      ParameterCheck.mandatoryString(PROP_TASKTYPE, taskType);
      
      wfBean.setupTaskDialog(taskId, taskType);
      
      NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
      navigationHandler.handleNavigation(fc, null, "dialog:manageTask");
      String viewId = fc.getViewRoot().getViewId();
      try
      {
         sc.getRequestDispatcher(BaseServlet.FACES_SERVLET + viewId).forward(req, res);
      }
      catch (Exception e)
      {
         throw new AlfrescoRuntimeException("Unable to forward to viewId: " + viewId, e);
      }
      
      return null;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.Command#getPropertyNames()
    */
   public String[] getPropertyNames()
   {
      return PROPERTIES;
   }
}

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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.app.servlet.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wizard.WizardManager;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;

/**
 * Command to execute the Create Web Content wizard via url.
 * <p>
 * Arguments: webproject = the GUID of the webproject to create the content in
 *            sandbox = the sandbox to create the content in
 *            form = optional form name as the default selection in the content wizard
 * 
 * @author Kevin Roast
 */
public class CreateWebContentCommand extends BaseUIActionCommand
{
   public static final String PROP_WEBPROJECTID = "webproject";
   public static final String PROP_SANDBOX = "sandbox";
   public static final String PROP_FORMNAME = "form";    // optional
   
   private static final String[] PROPERTIES = new String[] {
      PROP_SERVLETCONTEXT, PROP_REQUEST, PROP_RESPONSE, PROP_WEBPROJECTID, PROP_SANDBOX, PROP_FORMNAME};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      ServletContext sc = (ServletContext)properties.get(PROP_SERVLETCONTEXT);
      ServletRequest req = (ServletRequest)properties.get(PROP_REQUEST);
      ServletResponse res = (ServletResponse)properties.get(PROP_RESPONSE);
      FacesContext fc = FacesHelper.getFacesContext(req, res, sc);
      AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
      NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
      
      // setup context from url args in properties map
      String webProjectId = (String)properties.get(PROP_WEBPROJECTID);
      ParameterCheck.mandatoryString(PROP_WEBPROJECTID, webProjectId);
      String sandbox = (String)properties.get(PROP_SANDBOX);
      ParameterCheck.mandatoryString(PROP_SANDBOX, sandbox);
      navigator.setCurrentNodeId(webProjectId);
      avmBrowseBean.setSandbox(sandbox);
      
      // form name is optional, but if set we need to init the wizard manager with params
      String formName = (String)properties.get(PROP_FORMNAME);
      if (formName != null && formName.length() != 0)
      {
         WizardManager manager = (WizardManager)FacesHelper.getManagedBean(fc, WizardManager.BEAN_NAME);
         Map<String, String> params = new HashMap<String, String>(1, 1.0f);
         params.put(UIUserSandboxes.PARAM_FORM_NAME, formName);
         manager.setupParameters(params);
      }
      
      NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
      navigationHandler.handleNavigation(fc, null, "wizard:createWebContent");
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

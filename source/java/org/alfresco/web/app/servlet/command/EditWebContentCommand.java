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

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Command to execute the Edit Web Content (xforms) wizard via url.
 * <p>
 * Arguments: webproject = the GUID of the webproject to create the content in
 *            sandbox = the sandbox to create the content in
 *            path = path to the form instance data for edit
 * 
 * @author Kevin Roast
 */
public class EditWebContentCommand extends BaseUIActionCommand
{
   public static final String PROP_WEBPROJECTID = "webproject";
   public static final String PROP_SANDBOX = "sandbox";
   public static final String PROP_PATH = "path";
   
   private static final String[] PROPERTIES = new String[] {
      PROP_SERVLETCONTEXT, PROP_REQUEST, PROP_RESPONSE, PROP_WEBPROJECTID, PROP_SANDBOX, PROP_PATH};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      ServletContext sc = (ServletContext)properties.get(PROP_SERVLETCONTEXT);
      ServletRequest req = (ServletRequest)properties.get(PROP_REQUEST);
      ServletResponse res = (ServletResponse)properties.get(PROP_RESPONSE);
      FacesContext fc = FacesHelper.getFacesContext(req, res, sc);
//      AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);     // WCM
      NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
      
      // setup context from url args in properties map
      String webProjectId = (String)properties.get(PROP_WEBPROJECTID);
      ParameterCheck.mandatoryString(PROP_WEBPROJECTID, webProjectId);
      String sandbox = (String)properties.get(PROP_SANDBOX);
      ParameterCheck.mandatoryString(PROP_SANDBOX, sandbox);
      String path = (String)properties.get(PROP_PATH);
      ParameterCheck.mandatoryString(PROP_PATH, path);
      navigator.setCurrentNodeId(webProjectId);
//      // WCM
//      avmBrowseBean.setSandbox(sandbox);
//      // navigation handler is called directly in this method
//      avmBrowseBean.setupEditAction(path);
      
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

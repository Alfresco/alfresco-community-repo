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

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 * @author Kevin Roast
 */
public final class FacesHelper
{
   /**
    * Private constructor
    */
   private FacesHelper()
   {
   }
   
   /**
    * Return a valid FacesContext for the specific context, request and response.
    * The FacesContext can be constructor for Servlet and Portlet use.
    * 
    * @param context       ServletContext or PortletContext
    * @param request       ServletRequest or PortletRequest
    * @param response      ServletReponse or PortletResponse
    * 
    * @return FacesContext
    */
   public static FacesContext getFacesContext(Object context, Object request, Object response)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext != null) return facesContext;
      
      FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
      LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
      Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
      
      // Doesn't set this instance as the current instance of FacesContext.getCurrentInstance
      facesContext = contextFactory.getFacesContext(context, request, response, lifecycle);
      
      // Set using our inner class
      InnerFacesContext.setFacesContextAsCurrent(facesContext);
      
      // set a new viewRoot, otherwise context.getViewRoot returns null
      UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "/jsp/root");
      facesContext.setViewRoot(view);
      
      return facesContext;
   }
   
   /**
    * We need an inner class to be able to call FacesContext.setCurrentInstance
    * since it's a protected method
    */
   private abstract static class InnerFacesContext extends FacesContext
   {
      protected static void setFacesContextAsCurrent(FacesContext facesContext)
      {
         FacesContext.setCurrentInstance(facesContext);
      }
   }
}

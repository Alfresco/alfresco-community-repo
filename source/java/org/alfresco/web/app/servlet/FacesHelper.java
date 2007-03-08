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
package org.alfresco.web.app.servlet;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.bean.generator.IComponentGenerator;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public final class FacesHelper
{
   private static Log logger = LogFactory.getLog(FacesHelper.class);
   
   /**
    * Private constructor
    */
   private FacesHelper()
   {
   }
   
   /**
    * Return a valid FacesContext for the specific context, request and response.
    * The FacesContext can be constructor for Servlet use.
    * 
    * @param context       ServletContext
    * @param request       ServletRequest
    * @param response      ServletReponse
    * 
    * @return FacesContext
    */
   public static FacesContext getFacesContext(ServletRequest request, ServletResponse response, ServletContext context)
   {
      return getFacesContextImpl(request, response, context);
   }
   
   /**
    * Return a valid FacesContext for the specific context, request and response.
    * The FacesContext can be constructor for Servlet use.
    * 
    * @param context       ServletContext
    * @param request       ServletRequest
    * @param response      ServletReponse
    * 
    * @return FacesContext
    */
   public static FacesContext getFacesContext(PortletRequest request, PortletResponse response, PortletContext context)
   {
      return getFacesContextImpl(request, response, context);
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
   private static FacesContext getFacesContextImpl(Object request, Object response, Object context)
   {
      FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
      LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
      Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
      
      // Doesn't set this instance as the current instance of FacesContext.getCurrentInstance
      FacesContext facesContext = contextFactory.getFacesContext(context, request, response, lifecycle);
      
      // Set using our inner class
      InnerFacesContext.setFacesContextAsCurrent(facesContext);
      
      // set a new viewRoot, otherwise context.getViewRoot returns null
      UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "/jsp/root");
      facesContext.setViewRoot(view);
      
      return facesContext;
   }
   
   /**
    * Return a JSF managed bean reference.
    * 
    * @param fc      FacesContext
    * @param name    Name of the managed bean to return
    * 
    * @return the managed bean or null if not found
    */
   public static Object getManagedBean(FacesContext fc, String name)
   {
      ValueBinding vb = fc.getApplication().createValueBinding("#{" + name + "}");
      return vb.getValue(fc);
   }
   
   /**
    * Sets up the id for the given component, if the id is null a unique one
    * is generated using the standard Faces algorithm. If an id is present it
    * is checked for illegal characters.
    * 
    * @param context FacesContext
    * @param component The component to set the id for
    * @param id The id to set
    */
   public static void setupComponentId(FacesContext context, UIComponent component, String id)
   {
      if (id == null)
      {
         id = context.getViewRoot().createUniqueId();
      }
      else
      {
         // make sure we do not have illegal characters in the id
         id = makeLegalId(id);
      }
      
      component.setId(id);
   }

   /**
    * Makes the given id a legal JSF component id by replacing illegal
    * characters with underscores.
    * 
    * @param id The id to make legal
    * @return The legalised id
    */
   public static String makeLegalId(String id)
   {
      if (id != null)
      {
         // replace illegal ID characters with an underscore
         id = id.replace(':', '_');
         id = id.replace(' ', '_');
         
         // TODO: check all other illegal characters - only allowed dash and underscore
      }
      
      return id;
   }
   
   /**
    * Retrieves the named component generator implementation.
    * If the named generator is not found the TextFieldGenerator is looked up
    * as a default, if this is also not found an AlfrescoRuntimeException is thrown.
    * 
    * @param context FacesContext
    * @param generatorName The name of the component generator to retrieve
    * @return The component generator instance
    */
   public static IComponentGenerator getComponentGenerator(FacesContext context, String generatorName)
   {
      IComponentGenerator generator = lookupComponentGenerator(context, generatorName);
      
      if (generator == null)
      {
         // create a text field if we can't find a component generator (a warning should have already been
         // displayed on the appserver console)
         
         logger.warn("Attempting to find default component generator '" + RepoConstants.GENERATOR_TEXT_FIELD + "'");
         generator = lookupComponentGenerator(context, RepoConstants.GENERATOR_TEXT_FIELD);
      }
      
      // if we still don't have a component generator we should abort as vital configuration is missing
      if (generator == null)
      {
         throw new AlfrescoRuntimeException("Failed to find a component generator, please ensure the '" +
               RepoConstants.GENERATOR_TEXT_FIELD + "' bean is present in your configuration");
      }
      
      return generator;
   }
   
   private static IComponentGenerator lookupComponentGenerator(FacesContext context, String generatorName)
   {
      IComponentGenerator generator = null;
      
      Object obj = FacesHelper.getManagedBean(context, generatorName);
      if (obj != null)
      {
         if (obj instanceof IComponentGenerator)
         {
            generator = (IComponentGenerator)obj;
            
            if (logger.isDebugEnabled())
               logger.debug("Found component generator for '" + generatorName + "': " + generator);
         }
         else
         {
            logger.warn("Bean '" + generatorName + "' does not implement IComponentGenerator");
         }
      }
      else
      {
         logger.warn("Failed to find component generator with name of '" + generatorName + "'");
      }
      
      return generator;
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

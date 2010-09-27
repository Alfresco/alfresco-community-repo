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
package org.alfresco.web.app.servlet;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.EvaluationException;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
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
   /** Root browse screen JSF view ID */
   public static final String BROWSE_VIEW_ID = "/jsp/browse/browse.jsp";
   
   private static Log logger = LogFactory.getLog(FacesHelper.class);

   /**
    * Mask for hex encoding
    */
   private static final int MASK = (1 << 4) - 1;

   /**
    * Digits used for hex string encoding
    */
   private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


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
      return getFacesContextImpl(request, response, context, null);
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
   public static FacesContext getFacesContext(ServletRequest request, ServletResponse response, ServletContext context, String viewRoot)
   {
      return getFacesContextImpl(request, response, context, viewRoot);
   }
   
   /**
    * Return a valid FacesContext for the specific context, request and response.
    * The FacesContext can be constructor for Servlet use.
    * 
    * @param context       PortletContext
    * @param request       PortletRequest
    * @param response      PortletResponse
    * 
    * @return FacesContext
    */
   public static FacesContext getFacesContext(Object request, Object response, Object context)
   {
      return getFacesContextImpl(request, response, context, null);
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
   private static FacesContext getFacesContextImpl(Object request, Object response, Object context, String viewRoot)
   {
      FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
      LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
      Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

      // Doesn't set this instance as the current instance of FacesContext.getCurrentInstance
      FacesContext facesContext = contextFactory.getFacesContext(context, request, response, lifecycle);

      // Set using our inner class
      InnerFacesContext.setFacesContextAsCurrent(facesContext);

      // set a new viewRoot, otherwise context.getViewRoot returns null
      if (viewRoot == null)
      {
         viewRoot = FacesHelper.BROWSE_VIEW_ID;
      }
      
      UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, viewRoot);
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
      Object obj = null;
      
      try
      {
         ValueBinding vb = fc.getApplication().createValueBinding("#{" + name + "}");
         obj = vb.getValue(fc);
      }
      catch (EvaluationException ee)
      {
         // catch exception to resolve ADB-158/ACT-7343
         // not much we can do here, just make sure return is null
         if (logger.isDebugEnabled())
             logger.debug("Failed to resolve managed bean: " + name, ee);
         obj = null;
      }
      
      return obj;
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
    * Makes the given id a legal JSF component id by replacing illegal characters
    * with ISO9075 encoding - which itself a subset of valid HTML ID characters.
    * 
    * @param id   The id to make legal
    * 
    * @return the legalised id
    */
   public static String makeLegalId(String id)
   {
      return (id != null ? validFacesId(id) : null);
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

   /**
    * Helper to ensure only valid and acceptable characters are output as Faces component IDs.
    * Based on ISO9075 encoding - which itself a subset of valid HTML ID characters.
    */
   private static String validFacesId(String id)
   {
      int len = id.length();
      StringBuilder buf = new StringBuilder(len + (len>>1));
      for (int i = 0; i<len; i++)
      {
         char c = id.charAt(i);
         int ci = (int)c;
         if (i == 0)
         {
            if ((ci >= 65 && ci <= 90) ||    // A-Z
                (ci >= 97 && ci <= 122))     // a-z                 
            {
               buf.append(c);
            }
            else
            {
               encode(c, buf);
            }
         }
         else
         {
            if ((ci >= 65 && ci <= 90) ||    // A-Z
                (ci >= 97 && ci <= 122) ||   // a-z
                (ci >= 48 && ci <= 57) ||    // 0-9
                ci == 45 || ci == 95)        // - and _
            {
               buf.append(c);
            }
            else
            {
               encode(c, buf);
            }
         }
      }
      return buf.toString();
   }

   private static void encode(char c, StringBuilder builder)
   {
      char[] buf = new char[] { 'x', '0', '0', '0', '0', '_' };
      int charPos = 5;
      do
      {
         buf[--charPos] = DIGITS[c & MASK];
         c >>>= 4;
      }
      while (c != 0);
      builder.append(buf);
   }
}

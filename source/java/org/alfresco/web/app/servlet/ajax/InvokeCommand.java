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
package org.alfresco.web.app.servlet.ajax;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

/**
 * Command that invokes the method represented by the expression.
 * <p>
 * The managed bean method called is responsible for writing the response
 * by getting hold of the JSF ResponseWriter. Parameters can also be 
 * retrieved via the JSF ExternalContext object.
 * <p>
 * In a future release (if required) annotations may be used to state
 * what content type to use for the response.
 * 
 * @author gavinc
 */
public class InvokeCommand extends BaseAjaxCommand
{

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Annotation for a bean method that handles an ajax request.
    */
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.METHOD)
   public @interface ResponseMimetype
   {
      // NOTE: replaced Mimetype.MIMETYPE_XML with string literal due to bug
      //        http://bugs.sun.com/view_bug.do?bug_id=6512707 - causing build to fail
      public String value() default "text/xml";
   }

   /////////////////////////////////////////////////////////////////////////////

   public void execute(final FacesContext facesContext, 
                       final String expression,
                       final HttpServletRequest request, 
                       final HttpServletResponse response)
      throws ServletException, IOException
   {
      ResponseWriter writer = null;
      try
      {
         final int indexOfDot = expression.indexOf('.');
         final String variableName = expression.substring(0, indexOfDot);
         final String methodName = expression.substring(indexOfDot + 1);
         
         if (logger.isDebugEnabled())
            logger.debug("Invoking method represented by " + expression +
                         " on variable " + variableName + 
                         " with method " + methodName);

         Object bean = null;         
         if (Application.inPortalServer())
         {
            // retrieve the managed bean, this is really weak but if the 
            // request comes from a portal server the bean we need to get
            // is in the session with a prefix chosen by the portal vendor,
            // to cover this scenario we have to go through the names of
            // all the objects in the session to find the bean we want.
            
            String beanNameSuffix = "?" + variableName;
            Enumeration<?> enumNames = request.getSession().getAttributeNames();
            while (enumNames.hasMoreElements())
            {
               String name = (String)enumNames.nextElement();
               if (name.endsWith(beanNameSuffix))
               {
                  bean = request.getSession().getAttribute(name);
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Found bean " + bean + " in the session");
                  
                  break;
               }
            }
         }
         
         // if we don't have the bean yet try and get it via the variable resolver
         if (bean == null)
         {
            VariableResolver vr = facesContext.getApplication().getVariableResolver();
            bean = vr.resolveVariable(facesContext, variableName);
            
            if (logger.isDebugEnabled())
               logger.debug("Created bean " + bean + " via the variable resolver");
         }
         
         final Method method = bean.getClass().getMethod(methodName);

         final String responseMimetype = 
            (method.isAnnotationPresent(ResponseMimetype.class)
             ? method.getAnnotation(ResponseMimetype.class).value()
             : MimetypeMap.MIMETYPE_XML);

         if (logger.isDebugEnabled())
            logger.debug("invoking method " + method + 
                         " with repsonse mimetype  " + responseMimetype);
         
         writer = this.setupResponseWriter(responseMimetype,
                                           response,
                                           facesContext);

         // setup the transaction
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
         final Object beanFinal = bean;
         RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
         {
            public Object execute() throws Throwable
            {
               // invoke the method
               try
               {
                  method.invoke(beanFinal);
                  return null;
               }
               // Let's prevent RuntimeExceptions being wrapped twice by unwrapping InvocationTargetExceptions
               catch (InvocationTargetException e)
               {
                  if (e.getCause() != null)
                  {
                     throw e.getCause();
                  }
                  throw e;
               }
            }
         };
         txnHelper.doInTransaction(callback);
      }
      catch (EvaluationException e)
      {
         Throwable err = e.getCause();
         if (err == null)
         {
            logger.error("Failed to execute method " + expression + ": " + e.getMessage(), e);
            throw e;
         }
         else
         {
            logger.error("Failed to execute method " + expression + ": " + err.getMessage(), err);
            if (err instanceof RuntimeException)
            {
               throw (RuntimeException)err;
            }
            else
            {
               throw new AlfrescoRuntimeException("Failed to execute method " + expression + ": " + err.getMessage(), err);            
            }
         }
      }
      catch (RuntimeException err)
      {

         logger.error("Failed to execute method " + expression + ": " + err.getMessage(), err);
         throw err;
      }
      catch (Exception err)
      {

         logger.error("Failed to execute method " + expression + ": " + err.getMessage(), err);
         throw new AlfrescoRuntimeException("Failed to execute method " + expression + ": " + err.getMessage(), err);
      }

      // force the output back to the client
      writer.close();
   }

   /** setup the JSF response writer. */
   private ResponseWriter setupResponseWriter(final String mimetype,
                                              final HttpServletResponse response, 
                                              final FacesContext facesContext)
      throws IOException
   {
      final OutputStream os = response.getOutputStream();
      final UIViewRoot viewRoot = facesContext.getViewRoot();
      final RenderKitFactory renderFactory = (RenderKitFactory)
         FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
      final RenderKit renderKit = 
         renderFactory.getRenderKit(facesContext, viewRoot.getRenderKitId());
      final ResponseWriter writer = 
         renderKit.createResponseWriter(new OutputStreamWriter(os, "UTF-8"), 
                                        mimetype, 
                                        "UTF-8");
      facesContext.setResponseWriter(writer);
      // must be text/xml otherwise IE doesn't parse the response properly into responseXML
      response.setContentType(mimetype);
      return writer;
   }
}

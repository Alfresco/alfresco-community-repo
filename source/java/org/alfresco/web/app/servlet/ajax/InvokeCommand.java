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
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
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
      public String value() default MimetypeMap.MIMETYPE_XML;
   }

   /////////////////////////////////////////////////////////////////////////////

   public void execute(final FacesContext facesContext, 
                       final String expression,
                       final HttpServletRequest request, 
                       final HttpServletResponse response)
      throws ServletException, IOException
   {
      UserTransaction tx = null;
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

         // retrieve the managed bean, this is really weak but if the 
         // request comes from a portal server the bean we need to get
         // is in the session with a prefix chosen by the portal vendor,
         // to cover this scenario we have to go through the names of
         // all the objects in the session to find the bean we want.
         Object bean = null;
         
         if (Application.inPortalServer())
         {
            Enumeration enumNames = request.getSession().getAttributeNames();
            while (enumNames.hasMoreElements())
            {
               String name = (String)enumNames.nextElement();
               if (name.endsWith(variableName))
               {
                  bean = request.getSession().getAttribute(name);
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Found bean " + bean + " in the session");
                  
                  break;
               }
            }
         }
         
         // if we didn't find the bean it may be a request scope bean, in which
         // case go through the variable resolver to create it.
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
         tx = Repository.getUserTransaction(facesContext);
         tx.begin();
         
         // invoke the method
         method.invoke(bean);

         // commit
         tx.commit();
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) { tx.rollback(); } } catch (Exception ex) { }
         
         if (err instanceof EvaluationException)
         {
            final Throwable cause = ((EvaluationException)err).getCause();
            if (cause != null)
            {
               err = cause;
            }
         }
         else if (err instanceof InvocationTargetException)
         {
            final Throwable cause = ((InvocationTargetException)err).getCause();
            if (cause != null)
            {
               err = cause;
            }
         }

         logger.error("Failed to execute method " + expression + ": " + err.getMessage(),
                      err);
         throw new AlfrescoRuntimeException("Failed to execute method " + expression + 
                ": " + err.getMessage(), err);
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
         renderKit.createResponseWriter(new OutputStreamWriter(os), 
                                        mimetype, 
                                        "UTF-8");
      facesContext.setResponseWriter(writer);
      // must be text/xml otherwise IE doesn't parse the response properly into responseXML
      response.setContentType(mimetype);
      return writer;
   }
}

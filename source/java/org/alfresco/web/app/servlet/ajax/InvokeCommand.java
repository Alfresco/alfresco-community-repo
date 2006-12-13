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

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
         final VariableResolver vr = facesContext.getApplication().getVariableResolver();

         final int indexOfDot = expression.indexOf('.');
         final String variableName = expression.substring(0, indexOfDot);
         final String methodName = expression.substring(indexOfDot + 1);

         if (logger.isDebugEnabled())
            logger.debug("Invoking method represented by " + expression +
                         " on variable " + variableName + 
                         " with method " + methodName);

         final Object bean  = vr.resolveVariable(facesContext, variableName);
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

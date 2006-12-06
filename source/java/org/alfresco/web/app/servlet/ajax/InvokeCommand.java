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

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
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
   public void execute(final FacesContext facesContext, 
         final String expression,
         final HttpServletRequest request, 
         final HttpServletResponse response)
         throws ServletException, IOException
   {
      // setup the JSF response writer.
      
      // NOTE: it doesn't seem to matter what the content type of the response is (at least with Dojo), 
      // it determines it's behaviour from the mimetype specified in the AJAX call on the client,
      // therefore, for now we will always return a content type of text/xml.
      // In the future we may use annotations on the method to be called to specify what content
      // type should be used for the response.
      // NOTE: JSF only seems to support XML and HTML content types by default so this will
      //       also need to be addressed if other content types need to be returned i.e. JSON.
      
      OutputStream os = response.getOutputStream();
      UIViewRoot viewRoot = facesContext.getViewRoot();
      RenderKitFactory renderFactory = (RenderKitFactory)FactoryFinder.
            getFactory(FactoryFinder.RENDER_KIT_FACTORY);
      RenderKit renderKit = renderFactory.getRenderKit(facesContext, 
            viewRoot.getRenderKitId());
      ResponseWriter writer = renderKit.createResponseWriter(
            new OutputStreamWriter(os), MimetypeMap.MIMETYPE_XML, "UTF-8");
      facesContext.setResponseWriter(writer);
      // must be text/xml otherwise IE doesn't parse the response properly into responseXML
      response.setContentType(MimetypeMap.MIMETYPE_XML);

      // create the JSF binding expression
      String bindingExpr = makeBindingExpression(expression);
      
      if (logger.isDebugEnabled())
         logger.debug("Invoking method represented by " + bindingExpr);
      
      UserTransaction tx = null;
      try
      {
         // create the method binding from the expression
         MethodBinding binding = facesContext.getApplication().createMethodBinding(
               bindingExpr, new Class[] {});
         
         if (binding != null)
         {
            // setup the transaction
            tx = Repository.getUserTransaction(facesContext);
            tx.begin();
            
            // invoke the method
            binding.invoke(facesContext, new Object[] {});
            
            // commit
            tx.commit();
         }
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

         logger.error(err);
         throw new AlfrescoRuntimeException("Failed to execute method " + expression + 
                ": " + err.getMessage(), err);
      }

      // force the output back to the client
      writer.close();
   }
}

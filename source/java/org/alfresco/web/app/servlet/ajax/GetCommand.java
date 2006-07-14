package org.alfresco.web.app.servlet.ajax;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.bean.repository.Repository;

/**
 * Command that executes the given value binding expression.
 * <p>
 * This command is intended to be used for calling existing managed 
 * bean methods. The result of the value binding is added to
 * the response as is i.e. by calling toString(). 
 * The content type of the response is always text/html.
 * 
 * @author gavinc
 */
public class GetCommand extends BaseAjaxCommand
{
   public void execute(FacesContext facesContext, String expression,
         HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      // create the JSF binding expression
      String bindingExpr = makeBindingExpression(expression);
      
      if (logger.isDebugEnabled())
         logger.debug("Retrieving value from value binding: " + bindingExpr);
      
      UserTransaction tx = null;
      try
      {
         // create the value binding
         ValueBinding binding = facesContext.getApplication().
               createValueBinding(bindingExpr);
         
         if (binding != null)
         {
            // setup the transaction
            tx = Repository.getUserTransaction(facesContext, true);
            tx.begin();
            
            // get the value from the value binding
            Object value = binding.getValue(facesContext);
            response.getWriter().write(value.toString());
            
            // commit
            tx.commit();
         }
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         
         throw new AlfrescoRuntimeException("Failed to retrieve value: " + err.getMessage(), err);
      }
   }
}

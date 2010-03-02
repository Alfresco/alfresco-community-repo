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
            if (value != null)
            {
               response.getWriter().write(value.toString());
            }
            
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

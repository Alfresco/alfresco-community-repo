package org.alfresco.web.app.servlet.ajax;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all Ajax based commands
 * 
 * @author gavinc
 */
public abstract class BaseAjaxCommand implements AjaxCommand
{
   protected static Log logger = LogFactory.getLog(AjaxServlet.AJAX_LOG_KEY);
   
   public String makeBindingExpression(String expression)
   {
      return "#{" + expression + "}";
   }
}

package org.alfresco.web.app.servlet.ajax;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for all Ajax commands executed by this servlet.
 * 
 * The method is responsible for invoking the underlying managed bean 
 * and dealing with the response.
 * 
 * @author gavinc
 */
public interface AjaxCommand
{
   /**
    * Invokes the relevant method on the bean represented by the given
    * expression. Parameters required to call the method can be retrieved
    * from the request. 
    * 
    * Currently the content type of the response will always be text/html, in the 
    * future sublcasses may provide a mechanism to allow the content type to be set
    * dynamically.
    * 
    * @param facesContext FacesContext
    * @param expression The binding expression
    * @param request The request
    * @param response The response
    */
   public abstract void execute(FacesContext facesContext, String expression,
         HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException;
}

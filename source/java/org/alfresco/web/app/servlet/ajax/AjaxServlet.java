package org.alfresco.web.app.servlet.ajax;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet responsible for processing AJAX requests.
 * 
 * The URL to the servlet should be in the form:
 * <pre>/alfresco/ajax/command/Bean.binding.expression</pre>
 * <p>
 * where 'command' is one of 'invoke', 'get' or 'set'.
 * <p>
 * TODO: Explain what the commands do...
 * <p>
 * Like most Alfresco servlets, the URL may be followed by a valid 'ticket' argument for authentication:
 * ?ticket=1234567890
 * 
 * @author gavinc
 */
public class AjaxServlet extends BaseServlet
{
   public static final String AJAX_LOG_KEY = "alfresco.ajax";
   
   protected enum Command { invoke, get, set};
   
   private static final long serialVersionUID = -7654769105419391840L;
   private static Log logger = LogFactory.getLog(AJAX_LOG_KEY);
   private static Log headersLogger = LogFactory.getLog(AJAX_LOG_KEY + ".headers");
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      try
      {
         String uri = request.getRequestURI();
         
         if (logger.isDebugEnabled())
         {
            String queryString = request.getQueryString();
            logger.debug("Processing URL: " + uri + 
                  ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
         }
         
         // dump the request headers
         if (headersLogger.isDebugEnabled())
         {
            Enumeration headers = request.getHeaderNames();
            while (headers.hasMoreElements())
            {
               String name = (String)headers.nextElement();
               headersLogger.debug(name + ": " + request.getHeader(name));
            }
         }
         
         // ************
         // TODO: Need to send in a flag to method to stop it from redirecting
         //       to login page, we can then throw an error in here!!
         
         AuthenticationStatus status = servletAuthenticate(request, response);
         if (status == AuthenticationStatus.Failure)
         {
            return;
         }
         
         uri = uri.substring(request.getContextPath().length());
         StringTokenizer t = new StringTokenizer(uri, "/");
         int tokenCount = t.countTokens();
         if (tokenCount < 3)
         {
            throw new AlfrescoRuntimeException("Servlet URL did not contain all required args: " + uri); 
         }
         
         // skip the servlet name
         t.nextToken();
         
         // retrieve the command from the URL
         String commandName = t.nextToken();
         
         // retrieve the binding expression from the URL
         String expression = t.nextToken();
         
         // setup the faces context
         FacesContext facesContext = FacesHelper.getFacesContext(request, response, getServletContext());
         
         // instantiate the relevant command
         AjaxCommand command = null;
         if (Command.invoke.toString().equals(commandName))
         {
            command = new InvokeCommand();
         }
         else if (Command.get.toString().equals(commandName))
         {
            command = new GetCommand();
         }
//         else if (Command.set.toString().equals(commandName))
//         {
//            command = new SetCommand();
//         }
         else
         {
            throw new AlfrescoRuntimeException("Unrecognised command received: " + commandName);
         }
         
         // execute the command
         command.execute(facesContext, expression, request, response);
      }
      catch (RuntimeException error)
      {
         handleError(response, error);
      }
   }
   
   /**
    * Handles any error that occurs during the execution of the servlet
    * 
    * @param response The response
    * @param cause The cause of the error
    */
   protected void handleError(HttpServletResponse response, RuntimeException cause)
      throws ServletException, IOException
   {
      // if we can send back the 500 error with the error from the top of the 
      // stack as the error status message.
      
      // NOTE: if we use the built in support for generating error pages for
      //       500 errors we can tailor the output for AJAX calls so that the
      //       body of the response can be used to show the error details.
      
      if (!response.isCommitted())
      {
         // dump the error if debugging is enabled
         if (logger.isDebugEnabled())
         {
            logger.error(cause);
            Throwable theCause = cause.getCause();
            if (theCause != null)
            {
               logger.error("caused by: ", theCause);
            }
         }
            
         // extract a message from the exception
         String msg = cause.getMessage();
         if (msg == null)
         {
            msg = cause.toString();
         }
         
         // send the error
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      }
      else
      {
         // the response has already been comitted, not much we can do but
         // let the error through and let the container deal with it
         throw cause;
      }
   }
}

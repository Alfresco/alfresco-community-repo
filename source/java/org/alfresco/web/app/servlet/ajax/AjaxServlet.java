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
import java.util.Enumeration;

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
 * See http://wiki.alfresco.com/wiki/AJAX_Support for details.
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
   private static Log perfLogger = LogFactory.getLog(AJAX_LOG_KEY + ".performance");
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(final HttpServletRequest request, 
                          final HttpServletResponse response)
      throws ServletException, IOException
   {
      request.setCharacterEncoding("utf-8");
      // set default character encoding for the response
      response.setCharacterEncoding("utf-8");
      response.setContentType("text/xml;charset=UTF-8");
      
      long startTime = 0;
      String uri = request.getRequestURI();      
      if (logger.isDebugEnabled())
      {
         final String queryString = request.getQueryString();
         logger.debug("Processing URL: " + uri + 
                      ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }
         
      // dump the request headers
      if (headersLogger.isDebugEnabled())
      {
         final Enumeration<?> headers = request.getHeaderNames();
         while (headers.hasMoreElements())
         {
            final String name = (String)headers.nextElement();
            headersLogger.debug(name + ": " + request.getHeader(name));
         }
      }

      try
      {
         
         // Make sure the user is authenticated, if not throw an error to return the 
         // 500 Internal Server Error code back to the client
         AuthenticationStatus status = servletAuthenticate(request, response, false);
         if (status == AuthenticationStatus.Failure)
         {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Access Denied: User not authenticated");
            return;
         }
         
         setNoCacheHeaders(response);
         
         uri = uri.substring(request.getContextPath().length() + "/".length());
         final String[] tokens = uri.split("/");
         if (tokens.length < 3)
         {
            throw new AlfrescoRuntimeException("Servlet URL did not contain all required args: " + uri); 
         }
         
         // retrieve the command from the URL
         final String commandName = tokens[1];
         // retrieve the binding expression from the URL
         final String expression = tokens[2];
         
         // setup the faces context
         final FacesContext facesContext = FacesHelper.getFacesContext(request, response, getServletContext());
         
         // start a timer
         if (perfLogger.isDebugEnabled())
            startTime = System.currentTimeMillis();
         
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
      finally
      {
         // measure the time taken
         if (perfLogger.isDebugEnabled())
         {
            perfLogger.debug("Time to execute command: " + (System.currentTimeMillis() - startTime) + "ms");
         }
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
         // ALF-9036. We need to trap incomplete sessions
         if (cause instanceof IllegalStateException)
         {
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED, cause.getMessage());
         }
         else
         {
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
         }
      }
      else
      {
         // the response has already been comitted, not much we can do but
         // let the error through and let the container deal with it
         throw cause;
      }
   }
}

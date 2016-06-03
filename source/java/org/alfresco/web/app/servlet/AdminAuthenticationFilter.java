package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet filter is used to restrict direct URL access to administration
 * resource in the web client, for example the admin and jBPM consoles.
 * 
 * @author gavinc
 * @deprecated 5.0 not exposed in web-client web.xml
 */
public class AdminAuthenticationFilter implements Filter
{
   private static final Log logger = LogFactory.getLog(AdminAuthenticationFilter.class);
   
   private FilterConfig config;

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
      HttpServletRequest httpRequest  = (HttpServletRequest)req;
      HttpServletResponse httpResponse = (HttpServletResponse)res;
      
      // The fact that this filter is being called means a request for a protected
      // resource has taken place, check that the current user is in fact an
      // administrator.
      
      if (logger.isDebugEnabled())
         logger.debug("Authorising request for protected resource: " + httpRequest.getRequestURI());
      
      // there should be a user at this point so retrieve it
      User user = AuthenticationHelper.getUser(this.config.getServletContext(), httpRequest, httpResponse);
      
      // if the user is present check to see whether it is an admin user
      boolean isAdmin = (user != null && user.isAdmin());
      
      if (isAdmin)
      {
         if (logger.isDebugEnabled())
            logger.debug("Current user has admin authority, allowing access.");
         
         // continue filter chaining if current user is admin user
         chain.doFilter(req, res);
      }
      else
      {
         // return the 401 Forbidden error as the current user is not an administrator
         // if the response has already been committed there's nothing we can do but
         // print out a warning
         if (httpResponse.isCommitted() == false)
         {
            if (logger.isDebugEnabled())
               logger.debug("Current user does not have admin authority, returning 401 Forbidden error...");
            
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
         }
         else
         {
            if (logger.isWarnEnabled())
               logger.warn("Access denied to '" + httpRequest.getRequestURI() + 
                     "'. The response has already been committed so a 401 Forbidden error could not be sent!");
         }
      }
   }
   
   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {
      this.config = config;
   }
   
   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
      // nothing to do
   }
}

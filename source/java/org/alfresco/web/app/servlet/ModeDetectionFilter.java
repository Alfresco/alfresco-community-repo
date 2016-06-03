package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.web.app.Application;


/**
 * Filter that determines whether the application is running inside a portal
 * server or servlet engine. The fact that this filter gets called means
 * the application is running inside a servlet engine. 
 *  
 * @author gavinc
 * @deprecated 5.0 not exposed in web-client web.xml
 */
public class ModeDetectionFilter implements Filter
{  
   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {
      // nothing to do
   }

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
      // as we get here means we are inside a servlet engine as portal servers 
      // do not support the calling of filters yet
      Application.setInPortalServer(false);
      
      // continue filter chaining
      chain.doFilter(req, res);
   }

   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
      // nothing to do
   }
}

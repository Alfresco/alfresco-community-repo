/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.Application;

/**
 * @author Kevin Roast
 * 
 * Servlet filter responsible for redirecting to the login page for the Web Client if the user
 * does not have a valid ticket.
 * <p>
 * The current ticker is validated for each page request and the login page is shown if the
 * ticker has expired.
 * <p>
 * Note that this filter is only active when the system is running in a servlet container -
 * the AlfrescoFacesPortlet will be used for a JSR-168 Portal environment.
 */
public class AuthenticationFilter implements Filter
{
   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {
      this.context = config.getServletContext();
   }

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
      HttpServletRequest httpReq  = (HttpServletRequest)req;
      HttpServletResponse httpRes = (HttpServletResponse)res;
      
      // allow the login page to proceed
      if (httpReq.getRequestURI().endsWith(getLoginPage()) == false)
      {
         AuthenticationStatus status =
               AuthenticationHelper.authenticate(this.context, httpReq, httpRes, false);
         
         if (status == AuthenticationStatus.Success || status == AuthenticationStatus.Guest)
         {
            // continue filter chaining
            chain.doFilter(req, res);
         }
         else
         {
            
            // authentication failed - so end servlet execution and redirect to login page
            // also save the requested URL so the login page knows where to redirect too later
            BaseServlet.redirectToLoginPage(httpReq, httpRes, context);
         }
      }
      else
      {
         // continue filter chaining
         chain.doFilter(req, res);
      }
   }

   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
      // nothing to do
   }
   
   /**
    * @return The login page url
    */
   private String getLoginPage()
   {
      if (this.loginPage == null)
      {
         this.loginPage = Application.getLoginPage(this.context);
      }
      
      return this.loginPage;
   }
   
   
   private String loginPage = null;
   
   private ServletContext context;
}

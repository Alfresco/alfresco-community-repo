/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.scripts.Authenticator;
import org.alfresco.web.scripts.Description.RequiredAuthentication;
import org.alfresco.web.scripts.servlet.ServletAuthenticatorFactory;
import org.alfresco.web.scripts.servlet.WebScriptServletRequest;
import org.alfresco.web.scripts.servlet.WebScriptServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * In case of MT-context, this servlet filter will force authentication prior to WebScript binding, even for WebScripts
 * that do not require authentication.
 * 
 * In future releases, consider updating the HTTP API such that an optional tenant context could be specified as part of
 * the URL, hence not requiring pre-authentication in that case.
 */
public class MTWebScriptAuthenticationFilter implements Filter
{
    private FilterConfig config;
    
    private ApplicationContext appContext;
    
   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig config) throws ServletException
   {
      this.config = config;
      this.appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
   }

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
       if (AuthenticationUtil.isMtEnabled())
       {
           String currentUser = AuthenticationUtil.getCurrentUserName();
           if (currentUser == null)
           {
               // retrieve authenticator factory  
               String authenticatorId = config.getInitParameter("authenticator");
               if (authenticatorId != null && authenticatorId.length() > 0)
               {       
                   Object bean = appContext.getBean(authenticatorId);
                   if (bean == null || !(bean instanceof ServletAuthenticatorFactory))
                   {
                       throw new ServletException("Initialisation parameter 'authenticator' does not refer to a servlet authenticator factory (" + authenticatorId + ")");
                   }
                   
                   ServletAuthenticatorFactory authenticatorFactory = (ServletAuthenticatorFactory)bean;
                   
                   if ((req instanceof HttpServletRequest) && (res instanceof HttpServletResponse))
                   {
                       Authenticator authenticator = authenticatorFactory.create(new WebScriptServletRequest(null, (HttpServletRequest)req, null, null), new WebScriptServletResponse(null, (HttpServletResponse)res));
                       authenticator.authenticate(RequiredAuthentication.user, false);
                   }
               }
           }
       }
       
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

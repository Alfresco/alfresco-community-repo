<%--
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
--%>

<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="javax.transaction.UserTransaction" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.alfresco.service.transaction.TransactionService" %>
<%@ page import="org.alfresco.service.cmr.security.PermissionService" %>
<%@ page import="org.alfresco.service.cmr.security.AuthenticationService" %>
<%@ page import="org.alfresco.service.cmr.security.PersonService" %>
<%@ page import="org.alfresco.service.cmr.security.PermissionService" %>
<%@ page import="org.alfresco.service.cmr.repository.NodeRef" %>
<%@ page import="org.alfresco.repo.security.authentication.AuthenticationException" %>
<%@ page import="org.springframework.extensions.config.ConfigService" %>
<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.bean.NavigationBean" %>
<%@ page import="org.alfresco.web.bean.repository.User" %>
<%@ page import="org.alfresco.web.bean.repository.PreferencesService" %>
<%@ page import="org.alfresco.web.config.ClientConfigElement" %>

<%-- redirect to the web application's appropriate start page --%>
<%
// get the start location as configured by the web-client config
WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
ConfigService configService = (ConfigService)context.getBean("webClientConfigService");
ClientConfigElement configElement = (ClientConfigElement)configService.getGlobalConfig().getConfigElement("client");
String location = configElement.getInitialLocation();

AuthenticationService authService = (AuthenticationService)context.getBean("AuthenticationService");

// override with the users preference if they have one
User user = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
if (user != null)
{
   UserTransaction tx = ((TransactionService)context.getBean("TransactionService")).getUserTransaction();;
   tx.begin();
	try
	{
      authService.validate(user.getTicket());
      
      // ensure construction of the FacesContext before attemping a service call
      FacesContext fc = FacesHelper.getFacesContext(request, response, application);
      String preference = (String)PreferencesService.getPreferences(fc).getValue("start-location");
      if (preference != null)
      {
         location = preference;
      }
      
      tx.commit();
   }
   catch (AuthenticationException authErr)
   {
      try { tx.rollback(); } catch (Throwable tex) {}
      
      // expired ticket
      AuthenticationService unpAuth = (AuthenticationService)context.getBean("authenticationService");
      unpAuth.invalidateTicket(unpAuth.getCurrentTicket());
      unpAuth.clearCurrentSecurityContext();
   }
   catch (Throwable e)
   {
      try { tx.rollback(); } catch (Throwable tex) {}
   }
}
else
{
	UserTransaction tx = ((TransactionService)context.getBean("TransactionService")).getUserTransaction();;
   tx.begin();
	try
	{
	   authService.authenticateAsGuest();
		PersonService personService = (PersonService)context.getBean("personService");
      NodeRef guestRef = personService.getPerson(PermissionService.GUEST_AUTHORITY);
      user = new User(authService.getCurrentUserName(), authService.getCurrentTicket(), guestRef);
      session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
      
      // ensure construction of the FacesContext before attemping a service call
	   FacesContext fc = FacesHelper.getFacesContext(request, response, application);
	   String preference = (String)PreferencesService.getPreferences(session).getValue("start-location");
	   if (preference != null)
	   {
	      location = preference;
	   }
	   session.removeAttribute(AuthenticationHelper.AUTHENTICATION_USER);
      
      tx.commit();
   }
   catch (Throwable e)
   {
      try { tx.rollback(); } catch (Throwable tex) {}
   }
}

if (request.getMethod().equalsIgnoreCase("GET"))
{
   if (NavigationBean.LOCATION_MYALFRESCO.equals(location))
   {
      // Clear previous location - Fixes the issue ADB-61
      FacesContext fc = FacesHelper.getFacesContext(request, response, application);
      if (fc != null)
      {
         NavigationBean navigationBean = (NavigationBean)FacesHelper.getManagedBean(fc, "NavigationBean");
         if (navigationBean != null)
         {
            navigationBean.setLocation(null);
            navigationBean.setToolbarLocation(null);
         }
      }
      // Send redirect
      response.sendRedirect(request.getContextPath() + "/faces/jsp/dashboards/container.jsp");
   }
   else
   {
      response.sendRedirect(request.getContextPath() + "/faces/jsp/browse/browse.jsp");
   }
}
// route WebDAV requests
else if (request.getMethod().equalsIgnoreCase("PROPFIND") ||
         request.getMethod().equalsIgnoreCase("OPTIONS"))
{
   response.sendRedirect(request.getContextPath() + "/webdav/");
}
%>
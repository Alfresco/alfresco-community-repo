<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

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
<%@ page import="org.alfresco.config.ConfigService" %>
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

// override with the users preference if they have one
User user = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
if (user != null)
{
   // ensure construction of the FacesContext before attemping a service call
   FacesContext fc = FacesHelper.getFacesContext(request, response, application);
   String preference = (String)PreferencesService.getPreferences(fc).getValue("start-location");
   if (preference != null)
   {
      location = preference;
   }
}
else
{
	UserTransaction tx = ((TransactionService)context.getBean("TransactionService")).getUserTransaction();;
   tx.begin();
	try
	{
		AuthenticationService authService = (AuthenticationService)context.getBean("AuthenticationService");
	   authService.authenticateAsGuest();
		PersonService personService = (PersonService)context.getBean("personService");
      NodeRef guestRef = personService.getPerson(PermissionService.GUEST_AUTHORITY);
      user = new User(authService.getCurrentUserName(), authService.getCurrentTicket(), guestRef);
      
      // ensure construction of the FacesContext before attemping a service call
	   FacesContext fc = FacesHelper.getFacesContext(request, response, application);
	   String preference = (String)PreferencesService.getPreferences(user).getValue("start-location");
	   if (preference != null)
	   {
	      location = preference;
	   }
      
      tx.commit();
   }
   catch (Throwable e)
   {
      try { tx.rollback(); } catch (Throwable tex) {}
   }
}
if (NavigationBean.LOCATION_MYALFRESCO.equals(location))
{
   response.sendRedirect(request.getContextPath() + "/faces/jsp/dashboards/container.jsp");
}
else
{
   response.sendRedirect(request.getContextPath() + "/faces/jsp/browse/browse.jsp");
}
%>

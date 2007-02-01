<%--
Copyright (C) 2005 Alfresco, Inc.

Licensed under the Mozilla Public License version 1.1
with a permitted attribution clause. You may obtain a
copy of the License at

http://www.alfresco.org/legal/license.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific
language governing permissions and limitations under the
License.
--%>

<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.alfresco.service.cmr.security.PermissionService" %>
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
ConfigService configService = (ConfigService)WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext()).getBean("webClientConfigService");
ClientConfigElement configElement = (ClientConfigElement)configService.getGlobalConfig().getConfigElement("client");
String location = configElement.getInitialLocation();

// override with the users preference if they have one
User user = (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
if (user != null && (user.getUserName().equals(PermissionService.GUEST_AUTHORITY) == false))
{
   // ensure construction of the FacesContext before attemping a service call
   FacesContext fc = FacesHelper.getFacesContext(request, response, application);
   String preference = (String)PreferencesService.getPreferences(fc).getValue("start-location");
   if (preference != null)
   {
      location = preference;
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

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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.app.Application" %>

<% Application.getDashboardManager().initDashboard(); %>
<h:panelGrid columns="1" cellpadding="2" cellspacing="2" width="100%">
   <a:panel label="#{DashboardManager.dashletTitle[0]}" id="dashlet-1" rendered="#{DashboardManager.dashletAvailable[0]}"
            border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
      <f:subview id="dashlet-1-view">
			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(0)%>" />
		</f:subview>
   </a:panel>
   <a:panel label="#{DashboardManager.dashletTitle[1]}" id="dashlet-2" rendered="#{DashboardManager.dashletAvailable[1]}"
            border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
      <f:subview id="dashlet-2-view">
			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(1)%>" />
		</f:subview>
   </a:panel>
   <a:panel label="#{DashboardManager.dashletTitle[2]}" id="dashlet-3" rendered="#{DashboardManager.dashletAvailable[2]}"
            border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
      <f:subview id="dashlet-3-view">
			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(2)%>" />
		</f:subview>
   </a:panel>
   <a:panel label="#{DashboardManager.dashletTitle[3]}" id="dashlet-4" rendered="#{DashboardManager.dashletAvailable[3]}"
            border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
      <f:subview id="dashlet-4-view">
			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(3)%>" />
		</f:subview>
   </a:panel>
   <a:panel label="#{DashboardManager.dashletTitle[4]}" id="dashlet-5" rendered="#{DashboardManager.dashletAvailable[4]}"
            border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
      <f:subview id="dashlet-5-view">
			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(4)%>" />
		</f:subview>
   </a:panel>
</h:panelGrid>

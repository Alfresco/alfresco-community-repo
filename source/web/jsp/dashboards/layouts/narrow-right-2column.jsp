<%--
  Copyright (C) 2006 Alfresco, Inc.
 
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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.app.Application" %>

<h:panelGrid columns="2" cellpadding="2" cellspacing="2" width="100%" columnClasses="tableWideWidth,tableNarrowWidth">
   <% Application.getDashboardManager().initDashboard(); %>
   <h:panelGroup>
      <a:panel label="#{DashboardManager.dashletTitle[0]}" id="dashlet-0" rendered="#{DashboardManager.dashletAvailable[0]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-1-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(0)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[1]}" id="dashlet-1" rendered="#{DashboardManager.dashletAvailable[1]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-2-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(1)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[2]}" id="dashlet-2" rendered="#{DashboardManager.dashletAvailable[2]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-3-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(2)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[3]}" id="dashlet-3" rendered="#{DashboardManager.dashletAvailable[3]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-4-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(3)%>" />
   		</f:subview>
      </a:panel>
   </h:panelGroup>
   <h:panelGroup>
      <a:panel label="#{DashboardManager.dashletTitle[4]}" id="dashlet-4" rendered="#{DashboardManager.dashletAvailable[4]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-5-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(4)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[5]}" id="dashlet-5" rendered="#{DashboardManager.dashletAvailable[5]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-6-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(5)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[6]}" id="dashlet-6" rendered="#{DashboardManager.dashletAvailable[6]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-7-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(6)%>" />
   		</f:subview>
      </a:panel>
      <f:verbatim><div style='padding:4px'></div></f:verbatim>
      <a:panel label="#{DashboardManager.dashletTitle[7]}" id="dashlet-7" rendered="#{DashboardManager.dashletAvailable[7]}"
               border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE">
         <f:subview id="dashlet-8-view">
   			<jsp:include page="<%=Application.getDashboardManager().getDashletPage(7)%>" />
   		</f:subview>
      </a:panel>
   </h:panelGroup>
</h:panelGrid>

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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<f:verbatim>
<script type="text/javascript">

   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      if (document.getElementById("dialog:dialog-body:search-groups-text"))
      {
         document.getElementById("dialog:dialog-body:search-groups-text").focus();
         updateGroupsButtonState();
      }
   }
   
   function updateGroupsButtonState()
   {
      if (document.getElementById("dialog:dialog-body:search-groups-text").value.length == 0)
      {
         document.getElementById("dialog:dialog-body:search-groups-btn").disabled = true;
      }
      else
      {
         document.getElementById("dialog:dialog-body:search-groups-btn").disabled = false;
      }
   }

</script>
</f:verbatim>

<h:outputText value="<div style='padding-left: 8px; padding-top: 4px; padding-bottom: 4px'>" escape="false" />

<%-- Group Path Breadcrumb --%>
<a:breadcrumb value="#{DialogManager.bean.location}" styleClass="title" />

<h:outputText value="</div><div style='padding: 4px;'>" escape="false" />

   <%-- Groups List --%>
   <a:panel id="groups-panel" border="innerwhite" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.groups}">

   <%-- Groups Search Panel --%>
   <h:panelGroup rendered="#{DialogManager.bean.allowSearchGroups}">
      <f:verbatim>
         <%
             PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");
         %>
         <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
               <td valign=top style="padding-top: 2px" width=20></f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16" /><f:verbatim></td>
               <td class="mainSubText"></f:verbatim><h:outputText value="#{msg.group_search_info}" /><f:verbatim></td>
            </tr>
         </table>
         <%
             PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");
         %>
      </f:verbatim>
      <h:outputText value="</div><div style='padding: 8px;'>" escape="false" />
      <h:inputText id="search-groups-text" value="#{DialogManager.bean.groupsSearchCriteria}" size="35" maxlength="1024" onkeyup="updateGroupsButtonState();" onchange="updateGroupsButtonState();" />&nbsp;
         <h:commandButton id="search-groups-btn" value="#{msg.search}" action="#{DialogManager.bean.searchGroups}" disabled="true" />&nbsp;
         <h:commandButton value="#{msg.show_all}" action="#{DialogManager.bean.showAllGroups}" />
      <h:outputText value="</div><div style='padding: 4px;'>" escape="false" />
   </h:panelGroup>

   <a:richList id="groups-list" binding="#{DialogManager.bean.groupsRichList}" viewMode="#{DialogManager.bean.viewMode}" pageSize="12"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
            value="#{DialogManager.bean.groups}" var="r" initialSortColumn="name" initialSortDescending="true">
         
         <%-- Primary column for icons view mode --%>
         <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top" rendered="#{DialogManager.bean.viewMode == 'icons'}">
            <f:facet name="large-icon">
               <a:actionLink value="#{r.name}" image="/images/icons/group_large.gif" actionListener="#{DialogManager.bean.clickGroup}" showLink="false">
                  <f:param name="id" value="#{r.id}" />
               </a:actionLink>
            </f:facet>
            <a:actionLink value="#{r.name}" actionListener="#{DialogManager.bean.clickGroup}" styleClass="header">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </a:column>
         
         <%-- Primary column for details view mode --%>
         <a:column primary="true" style="padding:2px;text-align:left" rendered="#{DialogManager.bean.viewMode == 'details'}">
            <f:facet name="small-icon">
               <a:actionLink value="#{r.name}" image="/images/icons/group.gif" actionListener="#{DialogManager.bean.clickGroup}" showLink="false">
                  <f:param name="id" value="#{r.id}" />
               </a:actionLink>
            </f:facet>
            <f:facet name="header">
               <a:sortLink label="#{msg.identifier}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <a:actionLink value="#{r.name}" actionListener="#{DialogManager.bean.clickGroup}">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </a:column>
         
         <%-- Actions column --%>
         <a:column actions="true" style="text-align:left">
            <f:facet name="header">
               <h:outputText value="#{msg.actions}"/>
            </f:facet>
            <r:actions id="inline-group-actions" value="group_inline_actions" context="#{r}" showLink="false" styleClass="inlineAction" />
         </a:column>
         
         <a:dataPager/>
      </a:richList>
   </a:panel>

<h:outputText value="</div><div style='padding: 4px; margin-bottom: 6px;'>" escape="false" />

   <%-- Users in Group list --%>
   <a:panel id="users-panel" border="innerwhite" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.users}">

	<a:richList id="users-list" binding="#{DialogManager.bean.usersRichList}" viewMode="#{DialogManager.bean.viewMode}" pageSize="12"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
            value="#{DialogManager.bean.users}" var="r" initialSortColumn="userName" initialSortDescending="true">
         
         <%-- Primary column for icons view mode --%>
         <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;font-weight: bold;" rendered="#{DialogManager.bean.viewMode == 'icons'}">
            <f:facet name="large-icon">
               <h:graphicImage alt="#{r.name}" value="/images/icons/user_large.gif" />
            </f:facet>
            <h:outputText value="#{r.name}" />
         </a:column>
         
         <%-- Primary column for details view mode --%>
         <a:column primary="true" style="padding:2px;text-align:left;" rendered="#{DialogManager.bean.viewMode == 'details'}">
            <f:facet name="small-icon">
               <h:graphicImage alt="#{r.firstName}" value="/images/icons/person.gif" />
            </f:facet>
            <f:facet name="header">
               <a:sortLink label="#{msg.first_name}" value="firstName" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.firstName}" />
         </a:column>
         
         <%-- Last name column --%>
         <a:column width="120" style="text-align:left" rendered="#{DialogManager.bean.viewMode == 'details'}">
            <f:facet name="header">
               <a:sortLink label="#{msg.last_name}" value="lastName" styleClass="header" />
            </f:facet>
            <h:outputText value="#{r.lastName}" />
         </a:column>
         
         <%-- Username column --%>
         <a:column width="120" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.username}" value="userName" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.userName}" />
         </a:column>
         
         <%-- Actions column --%>
         <a:column actions="true" style="text-align:left">
            <f:facet name="header">
               <h:outputText value="#{msg.actions}"/>
            </f:facet>
            <a:actionLink value="#{msg.remove}" image="/images/icons/remove_user.gif" showLink="false" styleClass="inlineAction" actionListener="#{DialogManager.bean.removeUser}">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </a:column>
         
         <a:dataPager/>
      </a:richList>
   </a:panel>

<h:outputText value="</div>" escape="false" />
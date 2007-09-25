<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
--%><%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
         
<h:outputText value="<div style='padding-left: 8px; padding-top: 4px; padding-bottom: 4px'>" escape="false" />

<%-- Group Path Breadcrumb --%>
<a:breadcrumb value="#{DialogManager.bean.location}" styleClass="title" />

<h:outputText value="</div><div style='padding: 4px;'>" escape="false" />

   <%-- Groups List --%>
   <a:panel id="groups-panel" border="innerwhite" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.groups}">

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
            value="#{DialogManager.bean.users}" var="r" initialSortColumn="name" initialSortDescending="true">
         
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
               <h:graphicImage alt="#{r.name}" value="/images/icons/person.gif" />
            </f:facet>
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.name}" />
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
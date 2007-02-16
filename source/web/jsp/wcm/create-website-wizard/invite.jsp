<%--
Copyright (C) 2006 Alfresco, Inc.

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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:2px; padding-bottom:2px;" width="100%">
   <h:outputText styleClass="mainSubText" value="#{msg.specify_usersgroups}" />
   <h:outputText styleClass="mainSubText" value="1. #{msg.select_usersgroups}" />
   <a:genericPicker id="picker" showAddButton="false" filters="#{InviteWebsiteUsersWizard.filters}" queryCallback="#{InviteWebsiteUsersWizard.pickerCallback}" />
   <h:outputText value="#{msg.role}" />
   <h:selectOneListbox id="roles" style="width:250px" size="5">
      <f:selectItems value="#{InviteWebsiteUsersWizard.roles}" />
   </h:selectOneListbox>
   <h:panelGroup styleClass="mainSubText">
      <h:outputText value="2." /> <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{InviteWebsiteUsersWizard.addSelection}" styleClass="wizardButton" />
   </h:panelGroup>
   <h:outputText styleClass="mainSubText" value="#{msg.selected_usersgroups}" />
   <h:panelGroup>
      <h:dataTable value="#{InviteWebsiteUsersWizard.userRolesDataModel}" var="row" 
                   rowClasses="selectedItemsRow,selectedItemsRowAlt"
                   styleClass="selectedItems" headerClass="selectedItemsHeader"
                   cellspacing="0" cellpadding="4" 
                   rendered="#{InviteWebsiteUsersWizard.userRolesDataModel.rowCount != 0}">
         <h:column>
            <f:facet name="header">
               <h:outputText value="#{msg.name}" />
            </f:facet>
            <h:outputText value="#{row.label}" />
         </h:column>
         <h:column>
            <a:actionLink actionListener="#{InviteWebsiteUsersWizard.removeSelection}" image="/images/icons/delete.gif"
                          value="#{msg.remove}" showLink="false" style="padding:4px" />
         </h:column>
      </h:dataTable>
      
      <a:panel id="no-items" rendered="#{InviteWebsiteUsersWizard.userRolesDataModel.rowCount == 0}">
         <h:panelGrid columns="1" cellpadding="2" styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
            <h:outputText id="no-items-name" value="#{msg.name}" />
            <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
         </h:panelGrid>
      </a:panel>
   </h:panelGroup>
</h:panelGrid>

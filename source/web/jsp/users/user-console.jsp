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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<h:panelGrid columns="1" cellpadding="0" cellspacing="3" width="100%">
   <h:panelGroup>
      <h:panelGroup id="mydetails-panel-facets">
         <f:facet name="title" >
            <a:actionLink value="#{msg.modify}" action="dialog:editUserDetails"
                  showLink="false" image="/images/icons/Change_details.gif"
                  rendered="#{NavigationBean.isGuest == false}" />
         </f:facet>
      </h:panelGroup> 
      <a:panel label="#{msg.my_details}" id="mydetails-panel" 
            facetsId="dialog:dialog-body:mydetails-panel-facets" border="white" bgcolor="white"
            titleBorder="lbgrey" expandedTitleBorder="dotted"
            titleBgcolor="white">
         <h:panelGrid columns="2" columnClasses="alignTop,alignTop">
            <h:panelGrid columns="2" cellpadding="2" cellspacing="2" columnClasses="userPropertyLabel,userPropertyValue">
               <h:outputText value="#{msg.first_name}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.firstName}" />
               
               <h:outputText value="#{msg.last_name}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.lastName}" />
               
               <h:outputText value="#{msg.email}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.email}" />
               
               <h:outputText value="#{msg.user_description}:" styleClass="propertiesLabel" />
               <h:panelGroup rendered="#{!empty UsersBeanProperties.personDescription}">
                  <f:verbatim><div style="border: 1px solid #cccccc;padding:4px"></f:verbatim>
                     <h:outputText value="#{UsersBeanProperties.personDescription}" escape="false" />
                  <f:verbatim></div></f:verbatim>
               </h:panelGroup>
            </h:panelGrid>
            
            <h:panelGrid columns="2" cellpadding="2" cellspacing="2" columnClasses="userPropertyLabel,userPropertyValue">
               <h:outputText value="#{msg.user_organization}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.organization}" />
               
               <h:outputText value="#{msg.user_jobtitle}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.jobtitle}" />
               
               <h:outputText value="#{msg.user_location}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.location}" />
               
               <h:outputText value="#{msg.user_avatar}:" styleClass="propertiesLabel" />
               <h:panelGroup>
                  <h:panelGroup rendered="#{UsersBeanProperties.avatarUrl == null}">
                     <f:verbatim><div style="border: 2px solid #cccccc;width:120px;height:120px"></f:verbatim>
                  </h:panelGroup>
                  <h:panelGroup rendered="#{UsersBeanProperties.avatarUrl != null}">
                     <f:verbatim><div style="border: 2px solid #cccccc;width:120px;height:auto"></f:verbatim>
                  </h:panelGroup>
                  <h:graphicImage url="#{UsersBeanProperties.avatarUrl}" width="120" rendered="#{UsersBeanProperties.avatarUrl != null}" />
                  <f:verbatim></div></f:verbatim>
               </h:panelGroup>
            </h:panelGrid>
         </h:panelGrid>
         
         <%-- context for current user is setup on entry to user console --%>
         <a:actionLink id="change-password" value="#{msg.change_password}"
               action="dialog:changeMyPassword" style="padding-top:6px"
               image="/images/icons/change_password.gif"
               rendered="#{NavigationBean.isGuest == false}" />
      </a:panel>
   </h:panelGroup>
   
   <f:verbatim/>

   <a:panel label="#{msg.general_pref}" id="pref-panel"
         rendered="#{NavigationBean.isGuest == false || UserPreferencesBean.allowGuestConfig == true}"
         border="white" bgcolor="white" titleBorder="lbgrey"
         expandedTitleBorder="dotted" titleBgcolor="white">
      <h:panelGrid columns="2" cellpadding="2" cellspacing="2">
         <h:outputText value="#{msg.start_location}:" />
         <%-- Start Location drop-down selector --%>
         <h:selectOneMenu
               id="start-location" value="#{UserPreferencesBean.startLocation}"
               onchange="document.forms['dialog'].submit(); return true;">
            <f:selectItems value="#{UserPreferencesBean.startLocations}" />
         </h:selectOneMenu>
         
         <h:outputText value="#{msg.interface_language}:" />
         <h:selectOneMenu
               id="language" value="#{UserPreferencesBean.language}"
               onchange="document.forms['dialog'].submit(); return true;">
            <f:selectItems value="#{UserPreferencesBean.languages}" />
         </h:selectOneMenu>
         
         <h:outputText value="#{msg.content_language_filter}:" />
         <%-- Content Language Filter drop-down selector --%>
         <h:selectOneMenu
               id="content-filter-language"
               value="#{UserPreferencesBean.contentFilterLanguage}"
               onchange="document.forms['dialog'].submit(); return true;">
            <f:selectItems value="#{UserPreferencesBean.contentFilterLanguages}" />
         </h:selectOneMenu>
      </h:panelGrid>
   </a:panel>
   
   <f:verbatim/>

   <a:panel label="#{msg.user_management}" id="man-panel"
         rendered="#{NavigationBean.isGuest == false}" border="white"
         bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted"
         titleBgcolor="white">
      
      <a:panel id="usage-quota" rendered="#{UsersBeanProperties.usagesEnabled == true}">        
         <h:panelGrid columns="2" cellpadding="2" cellspacing="2">
            <h:outputText value="#{msg.sizeCurrent}:" styleClass="propertiesLabel" />
            <h:outputText value="#{UsersBeanProperties.userUsage}">
               <a:convertSize />
            </h:outputText>
            
            <h:outputText value="#{msg.sizeQuota}:" styleClass="propertiesLabel" />
            <h:outputText value="#{UsersBeanProperties.userQuota}">
               <a:convertSize />
            </h:outputText>
         </h:panelGrid>
      </a:panel>
      
      <h:panelGrid columns="2" cellpadding="2" cellspacing="2">
         <a:actionLink id="manage-deleted-items"
               value="#{msg.manage_deleted_items}"
               action="dialog:manageDeletedItems" 
               image="/images/icons/trashcan.gif" />
      </h:panelGrid>
   </a:panel>
</h:panelGrid>
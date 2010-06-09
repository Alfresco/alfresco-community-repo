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

<h:panelGrid columns="1" cellpadding="0" cellspacing="3" width="100%">
   <h:panelGroup>
      <h:panelGroup id="mydetails-panel-facets">
         <f:facet name="title" >
            <a:actionLink value="#{msg.modify}" action="dialog:editUserDetails"
                  showLink="false" image="/images/icons/Change_details.gif"
                  rendered="#{!NavigationBean.isGuest && NavigationBean.allowUserConfig}" />
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
               
               <h:outputText value="#{msg.presence_provider}:" styleClass="propertiesLabel" />
               <h:outputText value="#{UsersBeanProperties.person.properties.presenceProvider}" />
	
            	<h:outputText value="#{msg.presence_username}:" styleClass="propertiesLabel" />
            	<h:outputText value="#{UsersBeanProperties.person.properties.presenceUsername}" />
               
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
         
         <!-- custom properties for cm:person type -->
         <f:verbatim><div style="padding-bottom:12px"></f:verbatim>
         <r:propertySheetGrid id="person-props" value="#{UsersBeanProperties.person}"
               var="personProps" columns="1" mode="view" labelStyleClass="propertiesLabel"
               externalConfig="true" />
         <f:verbatim></div></f:verbatim>
         
         <%-- context for current user is setup on entry to user console --%>
         <a:actionLink id="change-password" value="#{msg.change_password}"
               action="dialog:changeMyPassword"
               image="/images/icons/change_password.gif"
               rendered="#{!NavigationBean.isGuest && NavigationBean.allowUserConfig && NavigationBean.allowUserChangePassword}" />
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
         
         <h:outputText value="#{msg.interface_language}:" rendered="#{LoginBean.languageSelect}" />
         <h:selectOneMenu
               id="language" value="#{UserPreferencesBean.language}" rendered="#{LoginBean.languageSelect}"
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
   
      <%-- Download automatically checkbox --%>
      <f:verbatim><br/></f:verbatim>
      
      <h:outputText value="#{msg.offline_editing}"/>
      
      <h:panelGrid cellpadding="2" columns="2" cellspacing="2">           
         <h:selectBooleanCheckbox
      		id="download-automatically"
      		value="#{UserPreferencesBean.downloadAutomatically}"
      		onchange="document.forms['dialog'].submit(); return true;"/>
      		
        <h:outputText value="#{msg.download_automatically}"/>
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
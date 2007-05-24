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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   window.onload = pageLoaded;
   var noItems;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:comment").focus();
      noItems = document.getElementById("dialog:finish-button").disabled;
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (noItems == false)
      {
         if (document.getElementById("dialog:dialog-body:comment").value.length == 0 ||
             document.getElementById("dialog:dialog-body:label").value.length == 0)
         {
            document.getElementById("dialog:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("dialog:finish-button").disabled = false;
         }
      }
   }
   
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.submit_submission_info}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px" 
             columnClasses=",,rightHandColumn">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.submit_comment}:"/>
   <h:inputText id="comment" value="#{DialogManager.bean.comment}" maxlength="1024" size="35"
        onkeyup="javascript:checkButtonState();" />
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.submit_snapshotlabel}:"/>
   <h:inputText id="label" value="#{DialogManager.bean.label}" maxlength="1024" size="35"
        onkeyup="javascript:checkButtonState();" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.workflow}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px">
   <h:panelGroup rendered="#{DialogManager.bean.workflowListSize != 0}">
      <h:outputText value="#{msg.submit_workflow_selection}" />
      <h:panelGrid columns="2" cellpadding="2" cellpadding="2">
         <a:selectList id="workflow-list" multiSelect="false" styleClass="noBrColumn" itemStyle="padding-top: 3px;"
               value="#{DialogManager.bean.workflowSelectedValue}">
            <a:listItems value="#{DialogManager.bean.workflowList}" />
         </a:selectList>
         <h:commandButton value="#{msg.submit_configure_workflow}" style="margin:4px" styleClass="dialogControls"
               action="dialog:submitConfigureWorkflow" actionListener="#{DialogManager.bean.setupConfigureWorkflow}" />
      </h:panelGrid>
   </h:panelGroup>
   <h:panelGroup rendered="#{DialogManager.bean.workflowListSize == 0}">
      <f:verbatim><% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %></f:verbatim>
      <h:panelGrid columns="2" cellpadding="0" cellpadding="0">
         <h:graphicImage url="/images/icons/warning.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
         <h:outputText styleClass="mainSubText" value="#{msg.submit_no_workflow_warning}" />
      </h:panelGrid>
      <f:verbatim><% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %></f:verbatim>
   </h:panelGroup>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:6px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.content_launch}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px"
             columnClasses="noBrColumn,rightHandColumn">
   <h:outputText value="#{msg.launch_date}:"/>
   <a:inputDatePicker id="launch-date" value="#{DialogManager.bean.launchDate}" 
                      initialiseIfNull="false" style="margin-right: 7px;" showTime="true" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:10px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.expiration_date_header}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px"
             columnClasses="noBrColumn,rightHandColumn" rendered="#{DialogManager.bean.enteringExpireDate}">
   <a:inputDatePicker id="default-expire-date" value="#{DialogManager.bean.defaultExpireDate}" 
                      initialiseIfNull="false" style="margin-right: 7px;" showTime="true" />
   <h:commandButton id="apply-expire-to-all" value="#{msg.apply_to_all}" style="margin-left:10px;"
                    actionListener="#{DialogManager.bean.applyDefaultExpireDateToAll}" />
</h:panelGrid>

<h:panelGrid columns="1" style="margin-left: 12px; margin-top: 6px;" cellpadding="0" cellspacing="0"
             rendered="#{DialogManager.bean.enteringExpireDate == false}">
	<h:commandLink value="#{msg.set_expiration_date}" actionListener="#{DialogManager.bean.enterExpireDate}" />
</h:panelGrid>

<h:panelGrid columns="1" style="margin-left: 12px; margin-top: 8px;" cellpadding="0" cellspacing="0">
	<h:outputText value="#{msg.change_expiration_date_change}" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.modified_items}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px">
   <h:panelGroup rendered="#{DialogManager.bean.warningItemsSize != 0}">
      <f:verbatim><% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %></f:verbatim>
      <h:panelGrid columns="2" cellpadding="0" cellpadding="0">
         <h:graphicImage url="/images/icons/warning.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
         <h:outputText styleClass="mainSubText" value="#{msg.submit_not_submit_warning}" />
      </h:panelGrid>
      <f:verbatim><% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %></f:verbatim>
      
      <a:richList id="warning-list" viewMode="details" value="#{DialogManager.bean.warningItems}" 
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
                  altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="name">
         
         <%-- Primary column for details view mode --%>
         <a:column id="col1" primary="true" width="100" style="padding:2px;text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <f:facet name="small-icon">
               <a:actionLink id="col1-icon1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.icon}" showLink="false" styleClass="inlineAction" rendered="#{!r.deleted}" />
            </f:facet>
            <a:actionLink id="col1-name1" value="#{r.name}" href="#{r.url}" target="new" rendered="#{!r.deleted}" />
            <h:outputText id="col1-name2" value="#{r.name}" rendered="#{r.deleted}" />
         </a:column>
         
         <%-- Description column --%>
         <a:column id="col2" width="200" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
            </f:facet>
            <h:outputText id="col2-desc" value="#{r.description}" />
         </a:column>
         
         <%-- Path column --%>
         <a:column id="col3" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.path}" value="path" styleClass="header"/>
            </f:facet>
            <h:outputText id="col3-path" value="#{r.path}" />
         </a:column>
         
         <%-- Modified Date column --%>
         <a:column id="col4" style="text-align:left; white-space:nowrap">
            <f:facet name="header">
               <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header"/>
            </f:facet>
            <h:outputText id="col4-date" value="#{r.modifiedDate}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <a:dataPager styleClass="pager" />
      </a:richList>
   </h:panelGroup>
   
   <h:panelGroup rendered="#{DialogManager.bean.submitItemsSize != 0}">
      <f:verbatim><% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %></f:verbatim>
      <h:panelGrid columns="2" cellpadding="0" cellpadding="0">
         <h:graphicImage url="/images/icons/info_icon.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
         <h:outputText styleClass="mainSubText" value="#{msg.submit_submit_info}" />
      </h:panelGrid>
      <f:verbatim><% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %></f:verbatim>
      
      <a:richList id="submit-list" viewMode="details" value="#{DialogManager.bean.submitItems}" 
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
                  altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="name">
         
         <%-- Primary column for details view mode --%>
         <a:column id="col10" primary="true" width="100" style="padding:2px;text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <f:facet name="small-icon">
               <h:panelGroup>
                  <a:actionLink id="col10-icon1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.icon}" showLink="false" styleClass="inlineAction" rendered="#{!r.deleted}" />
                  <h:graphicImage id="col10-icon2" title="#{r.name}" url="#{r.icon}" styleClass="inlineAction" rendered="#{r.deleted}" />
               </h:panelGroup>
            </f:facet>
            <a:actionLink id="col10-name1" value="#{r.name}" href="#{r.url}" target="new" rendered="#{!r.deleted}" />
            <h:outputText id="col10-name2" value="#{r.name}" rendered="#{r.deleted}" />
         </a:column>
         
         <%-- Description column --%>
         <a:column id="col11" width="200" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
            </f:facet>
            <h:outputText id="col11-desc" value="#{r.description}" />
         </a:column>
         
         <%-- Path column --%>
         <a:column id="col12" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.path}" value="path" styleClass="header"/>
            </f:facet>
            <h:outputText id="col12-path" value="#{r.path}" />
         </a:column>
         
         <%-- Modified Date column --%>
         <a:column id="col13" style="text-align:left; white-space:nowrap">
            <f:facet name="header">
               <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header"/>
            </f:facet>
            <h:outputText id="col13-date" value="#{r.modifiedDate}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <%-- Expiration Date column --%>
         <a:column id="col14" style="text-align:left; white-space:nowrap">
            <f:facet name="header">
               <a:sortLink label="#{msg.expiration_date}" value="expirationDate" styleClass="header" />
            </f:facet>
            <h:outputText id="col14-date" value="#{r.expirationDate}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <%-- Actions column --%>
         <a:column id="col15" actions="true" style="text-align:left">
            <f:facet name="header">
               <h:outputText id="col15-txt" value="#{msg.actions}"/>
            </f:facet>
            <a:actionLink value="#{msg.file_preview}" image="/images/icons/preview_website.gif" showLink="false"
                          href="#{r.previewUrl}" target="new" />
            <a:actionLink value="#{msg.change_expiration_date_title}" image="/images/icons/change_expire_date.gif" 
                          showLink="false" style="padding-left: 4px;" rendered="#{r.expirable}"
                          action="dialog:changeExpirationDate" actionListener="#{DialogManager.setupParameters}">
            	<f:param name="avmpath" value="#{r.fullPath}" />
            </a:actionLink> 
         </a:column>

         <a:dataPager styleClass="pager" />
      </a:richList>
   </h:panelGroup>
   
</h:panelGrid>

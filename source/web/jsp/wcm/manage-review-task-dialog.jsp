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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/wcm.tld" prefix="w" %>

<a:panel id="pooled-panel" label="#{msg.task_pooled_properties}" rendered="#{DialogManager.bean.pooledTask}" 
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">
   
   <r:propertySheetGrid id="pooled-task-props" value="#{DialogManager.bean.taskNode}" columns="1" >
      <r:property id="pooled-task-owner" readOnly="true" name="owner" />
      <r:association id="pooled-task-pool" readOnly="true" name="bpm:pooledActors" />
   </r:propertySheetGrid>

</a:panel>

<h:outputText id="padding1" styleClass="paddingRow" value="&nbsp;" escape="false" rendered="#{DialogManager.bean.pooledTask}" />

<a:panel id="metadata-panel" label="#{msg.task_properties}" 
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">
   
   <r:propertySheetGrid id="task-props" value="#{DialogManager.bean.taskNode}"
                       var="taskProps" columns="1" externalConfig="true" />
</a:panel>

<h:outputText id="padding3" styleClass="paddingRow" value="&nbsp;" escape="false" />

<h:panelGroup id="resources-panel-facets">
   <f:facet name="title">
      <h:panelGroup id="resources-actions" styleClass="deployReviewTaskActions">
         <a:actionLink id="resources-action1" value="#{msg.sandbox_deploy}" image="/images/icons/deploy.gif"
                       action="#{DialogManager.bean.deploy}" rendered="#{DialogManager.bean.testServersAvailable}" />
         <a:actionLink id="resources-action2" value="#{msg.deployment_report_action}" image="/images/icons/deployment_report.gif"
                       action="#{DialogManager.bean.viewDeployReport}" rendered="#{DialogManager.bean.deployAttempted}" />
         <a:actionLink id="resources-action3" value="#{msg.sandbox_release_test_server}" image="/images/icons/release_server.gif"
                       action="#{DialogManager.bean.releaseTestServer}" rendered="#{DialogManager.bean.testServerAllocated}" />
      </h:panelGroup>
   </f:facet>
</h:panelGroup>

<a:panel id="resources-panel" label="#{msg.resources}" facetsId="dialog-body:resources-panel-facets"
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" 
         styleClass="mainSubTitle">
   
   <h:outputText value="<div>#{msg.no_resources}</div>" rendered="#{empty DialogManager.bean.resources}" escape="false"/>
   
   <w:avmList  id="resources-list" viewMode="details" value="#{DialogManager.bean.resources}" var="r"
               binding="#{DialogManager.bean.packageItemsRichList}"
               styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
               altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
               initialSortColumn="name" initialSortDescending="true"
               rendered="#{not empty DialogManager.bean.resources}">
      
      <%-- Name column --%>
      <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
         </f:facet>
         <f:facet name="small-icon">
            <h:panelGroup>
               <a:actionLink id="col1-icon1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" 
                             showLink="false" styleClass="inlineAction" rendered="#{!r.deleted}" />
               <h:graphicImage id="col1-icon2" title="#{r.name}" url="#{r.fileType16}" styleClass="inlineAction" 
                               rendered="#{r.deleted}" />
            </h:panelGroup>
         </f:facet>
         <a:actionLink id="col1-name1" value="#{r.name}" href="#{r.url}" target="new" rendered="#{!r.deleted}" />
         <h:outputText id="col1-name2" value="#{r.name} [#{msg.avm_node_deleted}]" rendered="#{r.deleted}" />
         <r:lockIcon id="col1-lock" value="#{r.nodeRef}" align="absmiddle" />
      </a:column>
      
      <%-- Description column --%>
      <a:column id="col2" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col2-sort" label="#{msg.description}" value="description" styleClass="header"/>
         </f:facet>
         <h:outputText id="col2-txt" value="#{r.description}" />
      </a:column>
      
      <%-- Path column --%>
      <a:column id="col3" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col3-sort" label="#{msg.path}" value="path" styleClass="header"/>
         </f:facet>
         <r:nodePath id="col3-path" value="#{r.path}" action="dialog:close:browse" 
                     actionListener="#{BrowseBean.clickSpacePath}" />
      </a:column>

      <%-- Created Date column --%>
      <a:column id="col4" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header"/>
         </f:facet>
         <h:outputText id="col4-txt" value="#{r.created}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
      
      <%-- Modified Date column --%>
      <a:column id="col5" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col5-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
         </f:facet>
         <h:outputText id="col5-txt" value="#{r.modified}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
      
      <%-- Expiration Date column --%>
		<a:column id="col16" style="text-align:left; white-space:nowrap">
		   <f:facet name="header">
		      <a:sortLink label="#{msg.expiration_date}" value="expirationDate" styleClass="header" />
		   </f:facet>
		   <h:outputText id="col16-date" value='#{r["wca:expirationDate"]}'>
		      <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
		   </h:outputText>
		</a:column>
                        
      <%-- Actions column --%>
      <a:column id="col7" actions="true" style="padding:2px;text-align:left">
         <f:facet name="header">
            <h:outputText id="col7-txt" value="#{msg.actions}"/>
         </f:facet>
         <r:actions id="col7-actions" value="#{DialogManager.bean.packageItemActionGroup}" 
                    context="#{r}" showLink="false" styleClass="inlineAction" />
      </a:column>
      <a:dataPager styleClass="pager" id="pager1" />
   </w:avmList>

   <h:panelGrid id="package-actions-group" columns="1" styleClass="paddingRow">
      <r:actions id="package-actions" context="#{DialogManager.bean.taskNode}" 
                 value="#{DialogManager.bean.packageActionGroup}" />
   </h:panelGrid>
   
   <h:panelGrid id="add-item-control" columns="1" rendered="#{DialogManager.bean.itemBeingAdded}" 
                styleClass="selector" style="margin-top: 6px;">
      <r:contentSelector id="content-picker" value="#{DialogManager.bean.itemsToAdd}" styleClass="" />
      <h:panelGrid columns="2">
         <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{DialogManager.bean.addPackageItems}" />
         <h:commandButton id="cancel-button" value="#{msg.cancel}" actionListener="#{DialogManager.bean.cancelAddPackageItems}" />
      </h:panelGrid>
   </h:panelGrid>

</a:panel>

<h:outputText id="padding4" styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel id="workflow-summary-panel" label="#{msg.part_of_workflow}"
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">

   <r:workflowSummary id="workflow-summary" value="#{DialogManager.bean.workflowInstance}" styleClass="workflowSummary" />
   
   <f:verbatim><div style="padding-left:9px;padding-top:8px;padding-bottom:4px"></f:verbatim>
      <a:panel id="pending-submission-panel" label="#{msg.workflow_history}" progressive="true" 
            expanded="true" styleClass="mainSubTitle">
         <r:workflowHistory id="workflow-history" value="#{DialogManager.bean.workflowInstance}" styleClass="workflowHistoryList" />
      </a:panel>
   <f:verbatim></div></f:verbatim>
</a:panel>

<h:outputText id="padding5" styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel rendered="false" id="workflow-outline" label="#{msg.workflow_outline}" progressive="true" expanded="false" 
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">

   <h:graphicImage value="#{DialogManager.bean.workflowDefinitionImageUrl}"/>
   
</a:panel>

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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid id="grid-1" columns="1" cellpadding="2" width="100%">
   <%-- Workflow selection list - scrollable DIV area --%>
   <h:outputText id="msg-select" styleClass="mainSubText" value="#{msg.website_select_workflows}:" />
   <h:panelGroup id="grp-1">
      <f:verbatim><div style="height:144px;*height:148px;width:100%;overflow:auto" class='selectListTable'></f:verbatim>
      <a:selectList id="workflow-list" activeSelect="true" style="width:100%" itemStyleClass="selectListItem">
         <a:listItems id="items1" value="#{WizardManager.bean.workflowList}" />
         <h:commandButton value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{WizardManager.bean.addWorkflow}" />
      </a:selectList>
      <f:verbatim></div></f:verbatim>
   </h:panelGroup>
   
   <%-- Selected Workflow table, with configuration buttons and info text --%>
   <f:verbatim><div style='padding:4px'></div></f:verbatim>
   <h:outputText id="msg-selected" styleClass="mainSubText" value="#{msg.website_selected_workflows}:" />
   <h:dataTable id="items" value="#{WizardManager.bean.workflowsDataModel}" var="row" 
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4" width="100%"
                rendered="#{WizardManager.bean.workflowsDataModel.rowCount != 0}">
      <h:column id="col1">
         <f:facet name="header">
            <h:outputText id="title-01" value="#{msg.name}" />
         </f:facet>
         <h:outputText id="msg-01" value="#{row.title}" />
         <h:graphicImage id="img-01" url="/images/icons/warning.gif" style="padding:2px" width="16" height="16"
               rendered="#{row.params == null}" title="#{msg.workflow_not_configured}" />
      </h:column>
      <h:column id="col2">
         <f:facet name="header">
            <h:outputText id="title-02" value="#{msg.configure}" />
         </f:facet>
         <h:outputText id="msg-02" value="#{msg.website_filename_match}:" style="padding-right:4px" />
         <h:outputText id="msg-03" value="#{row.filenamePattern}" />
         <h:commandButton id="cmd-1" rendered="#{WizardManager.bean.editMode == false}" value="#{msg.form_template_conf_workflow}" style="margin-left:4px" styleClass="dialogControls" action="dialog:formTemplateWorkflow" actionListener="#{WizardManager.bean.setupWorkflowAction}" />
         <h:commandButton id="cmd-2" rendered="#{WizardManager.bean.editMode == true}" value="#{msg.form_template_conf_workflow}" style="margin-left:4px" styleClass="dialogControls" action="dialog:editFormTemplateWorkflow" actionListener="#{WizardManager.bean.setupWorkflowAction}" />
      </h:column>
      <h:column id="col3">
         <a:actionLink id="act-01" actionListener="#{WizardManager.bean.removeWorkflow}" image="/images/icons/delete.gif"
                       value="#{msg.remove}" showLink="false" style="padding:4px" />
      </h:column>
   </h:dataTable>
   
   <a:panel id="no-items" rendered="#{WizardManager.bean.workflowsDataModel.rowCount == 0}">
      <h:panelGrid id="grid-2" columns="1" cellpadding="2" width="100%"
            styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
</h:panelGrid>

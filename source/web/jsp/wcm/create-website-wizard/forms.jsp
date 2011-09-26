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
   <%-- Form selection list - scrollable DIV area --%>
   <h:outputText id="msg-select" styleClass="mainSubText" value="#{msg.website_select_form}:" />
   <h:panelGroup id="grp-1">
      <f:verbatim><div style="height:144px;*height:148px;width:100%;overflow:auto" class='selectListTable'></f:verbatim>
      <a:selectList id="form-list" activeSelect="true" style="width:100%" itemStyleClass="selectListItem">
         <a:listItems id="items1" value="#{WizardManager.bean.formsList}" />
         <h:commandButton value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{WizardManager.bean.addForm}" />
      </a:selectList>
      <f:verbatim></div></f:verbatim>
   </h:panelGroup>
   <h:commandButton id="cmd-create" value="#{msg.create_web_form}" style="margin:2px" styleClass="dialogControls" action="wizard:createWebForm" />
   
   <%-- Selected Form table, with configuration buttons and info text --%>
   <f:verbatim><div style='padding:4px'></div></f:verbatim>
   <h:dataTable id="items" value="#{WizardManager.bean.formsDataModel}" var="row" 
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4" width="100%"
                rendered="#{WizardManager.bean.formsDataModel.rowCount != 0}">
      <h:column id="col1">
         <f:facet name="header">
            <h:outputText id="head-1" value="#{msg.website_selected_forms}" />
         </f:facet>
         <f:verbatim>
            <img style="float:left" src="<%=request.getContextPath()%>/images/icons/webform_large.gif" />
         </f:verbatim>
         <h:panelGrid id="grid-form" columns="2" cellspacing="1">
            <h:outputText id="msg-01" value="#{msg.name}: " />
            <h:outputText id="msg-02" value="#{row.name}" />
            <h:outputText id="msg-14" value="#{msg.title}: " />
            <h:outputText id="msg-15" value="#{row.title}" />
            
            <h:outputText id="msg-03" value="#{msg.description}: " />
            <h:outputText id="msg-04" style="font-style:italic" rendered="#{empty row.description}" value="#{msg.description_not_set}" />
            <h:outputText id="msg-05" rendered="#{!empty row.description}" value="#{row.description}" />
            
            <h:outputText id="msg-06" value="#{msg.workflow}: " />
            <h:panelGroup id="grp-01" rendered="#{row.workflow != null}">
               <h:outputText id="msg-07" value="#{row.workflow.title}" />
               <h:graphicImage id="img-01" url="/images/icons/warning.gif" style="padding:2px" width="16" height="16"
                  rendered="#{row.workflow.params == null}" title="#{msg.workflow_not_configured}" />
            </h:panelGroup>
            <h:outputText id="msg-08" rendered="#{row.workflow == null}" value="#{msg.workflow_not_set}" />
            
            <h:outputText id="msg-09" value="#{msg.output_path_pattern}: " />
            <h:outputText id="msg-10" value="#{row.outputPathPattern}" />
            
            <h:outputText id="msg-11" value="#{msg.rendering_engines_selected}: " />
            <h:outputText id="msg-12" rendered="#{row.templates == null}" value="0" />
            <h:outputText id="msg-13" rendered="#{row.templates != null}" value="#{row.templatesSize}" />
         </h:panelGrid>
      </h:column>
      <h:column id="col2">
         <f:facet name="header">
            <h:outputText id="head-2" value="#{msg.actions}" />
         </f:facet>
         <h:panelGrid id="grid-3" columns="1" cellspacing="2" rendered="#{WizardManager.bean.editMode == false}">
            <h:commandButton id="cmd1-1" value="#{msg.form_template_details}" styleClass="dialogControls" action="dialog:formTemplateDetails" actionListener="#{WizardManager.bean.setupFormAction}" />
            <h:commandButton id="cmd1-2" value="#{msg.form_template_conf_workflow}" styleClass="dialogControls" action="dialog:formTemplateWorkflow" actionListener="#{WizardManager.bean.setupFormAction}" disabled="#{row.workflow == null}" />
            <h:commandButton id="cmd1-3" value="#{msg.form_template_select_templates}" styleClass="dialogControls" action="dialog:formTemplateTemplates" actionListener="#{WizardManager.bean.setupFormAction}" />
         </h:panelGrid>
         <h:panelGrid id="grid-4" columns="1" cellspacing="2" rendered="#{WizardManager.bean.editMode == true}">
            <h:commandButton id="cmd2-1" value="#{msg.form_template_details}" styleClass="dialogControls" action="dialog:editFormTemplateDetails" actionListener="#{WizardManager.bean.setupFormAction}" />
            <h:commandButton id="cmd2-2" value="#{msg.form_template_conf_workflow}" styleClass="dialogControls" action="dialog:editFormTemplateWorkflow" actionListener="#{WizardManager.bean.setupFormAction}" disabled="#{row.workflow == null}" />
            <h:commandButton id="cmd2-3" value="#{msg.form_template_select_templates}" styleClass="dialogControls" action="dialog:editFormTemplateTemplates" actionListener="#{WizardManager.bean.setupFormAction}" />
         </h:panelGrid>
      </h:column>
      <h:column>
         <a:actionLink id="act1" actionListener="#{WizardManager.bean.removeForm}" image="/images/icons/delete.gif"
                       value="#{msg.remove}" showLink="false" style="padding:4px" />
      </h:column>
   </h:dataTable>
   
   <a:panel id="no-items" rendered="#{WizardManager.bean.formsDataModel.rowCount == 0}">
      <h:panelGrid id="grid-none" width="100%" columns="1" cellpadding="2"
            styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
</h:panelGrid>

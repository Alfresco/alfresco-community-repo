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

<jsp:directive.page import="java.io.*"/>
<jsp:directive.page import="org.alfresco.web.bean.FileUploadBean"/>
<jsp:directive.page import="org.alfresco.web.bean.wcm.CreateFormWizard"/>
<jsp:directive.page buffer="32kb" contentType="text/html;charset=UTF-8"/>
<jsp:directive.page isELIgnored="false"/>

<f:verbatim>
  <script type="text/javascript" 
	src="<%=request.getContextPath()%>/scripts/upload_helper.js">
  </script>
  <script type="text/javascript">
  function handle_upload(target)
  {
    handle_upload_helper(target, 
                         "<%= CreateFormWizard.FILE_RENDERING_ENGINE_TEMPLATE %>", 
                         upload_complete,
                         "<%= request.getContextPath() %>")
  }

  function upload_complete(id, path, filename)
  {
    var rendering_engine_template_file_input = 
      document.getElementById("wizard:wizard-body:rendering-engine-template-file");
    rendering_engine_template_file_input.value = filename;
    rendering_engine_template_file_input.form.submit();
  }
  </script>
</f:verbatim>

<h:inputText id="rendering-engine-template-file" 
	     value="#{WizardManager.bean.renderingEngineTemplateFileName}" 
	     immediate="true"
	     style="display:none;"
	     valueChangeListener="#{WizardManager.bean.renderingEngineTemplateFileValueChanged}"/>

<h:panelGrid id="general-properties-panel-grid" 
	     columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%">
  <h:outputText id="step-1-text" 
		value="1. #{msg.create_form_configure_rendering_engine_templates_step1_desc}" 
		escape="false" />
  <h:panelGrid id="panel_grid_3"
               columns="4" cellpadding="3" cellspacing="3" border="0"
	       columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn">
               width="100%">
    <h:outputText id="required_image_rendering_engine_template_file"
                  value="" />
    <h:outputText id="output_text_rendering_engine_template_file"
                  value="#{msg.rendering_engine_template_file}:"/>
    <h:column id="column_rendering_engine_template_file_empty"
	      rendered="#{empty WizardManager.bean.renderingEngineTemplateFileName}">
      <f:verbatim><input id="wizard:wizard-body:file-input" 
			 type="file" 
			 size="35" 
			 name="alfFileInput" 
			 onchange="javascript:handle_upload(this)"/></f:verbatim>
      
    </h:column>
    <h:column id="column_rendering_engine_template_file_not_empty"
	      rendered="#{!empty WizardManager.bean.renderingEngineTemplateFileName}">
      <h:outputText id="rendering-engine-template-file-name"
                    value="#{WizardManager.bean.renderingEngineTemplateFileName}"/>
      <h:outputText id="output_text_rendering_engine_template_space"
                    value="&nbsp;"
		    escape="false"/>
      <a:actionLink id="action_link_remove_rendering_engine_template"
		    image="/images/icons/delete.gif" 
                    value="#{msg.remove}" 
                    action="#{WizardManager.bean.removeUploadedRenderingEngineTemplateFile}"
                    showLink="false" 
		    target="top"/>
    </h:column>
    <%-- we need to include this invisible image in order to get the column to size correctly --%>
    <h:graphicImage id="invisible_img_rendering_engine_template_file_help"
		    value="/images/icons/Help_icon.gif" style="cursor:help; visibility: hidden;"/>
  </h:panelGrid>

  <h:outputText id="step-2-text" 
		value="2. #{msg.create_form_configure_rendering_engine_templates_step2_desc}" 
		escape="false" />
  <h:panelGrid id="panel_grid_specify_details"
               columns="4" cellpadding="3" cellspacing="3" border="0"
	       columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn,panelGridRequiredImageColumn">
               width="100%">

    <h:graphicImage id="required-image-rendering-engine"
                    value="/images/icons/required_field.gif" 
                    alt="#{msg.required_field}" 
                    rendered="#{WizardManager.bean.renderingEngineTemplateFileName != null}"/>
    <h:outputText id="not-required-rendering-engine" 
                  value=""
                  rendered="#{WizardManager.bean.renderingEngineTemplateFileName == null}"/>
    <h:outputText id="rendering-engine-output-text"
                  value="#{msg.rendering_engine}:"/>
    <h:selectOneRadio id="rendering-engine" 
		      value="#{WizardManager.bean.renderingEngineName}"
                      disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}">
      <f:selectItems id="rendering-engine-choices"
		     value="#{WizardManager.bean.renderingEngineChoices}"/>
    </h:selectOneRadio>
    <h:column id="rendering-engine-help"/>

    <h:graphicImage id="required-image-name"
                    value="/images/icons/required_field.gif" 
                    alt="#{msg.required_field}" 
                    rendered="#{WizardManager.bean.renderingEngineTemplateFileName != null}"/>
    <h:outputText id="not-required-name" 
                  value=""
                  rendered="#{WizardManager.bean.renderingEngineTemplateFileName == null}"/>
    <h:outputText id="name-output-text"
                  value="#{msg.name}:"/>
    <h:inputText id="name" 
		 value="#{WizardManager.bean.renderingEngineTemplateName}"
                 disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}"
                 maxlength="1024" 
		 size="35"/>
    <h:column id="name-help"/>

    <h:outputText id="no_graphic_image_title" value=""/>
    <h:outputText id="title-output-text"
                  value="#{msg.title}:"/>
    <h:inputText id="title" 
		 value="#{WizardManager.bean.renderingEngineTemplateTitle}"
                 disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}"
                 maxlength="1024" 
		 size="35"/>
    <h:column id="title-help"/>

    <h:outputText id="no_graphic_image_description" value=""/>
    <h:outputText id="description-output-text"
                  value="#{msg.description}:"/>
    <h:inputText id="description" 
		 value="#{WizardManager.bean.renderingEngineTemplateDescription}"
                 disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}"
                 maxlength="1024" 
		 style="width:100%"/>
    <h:column id="description-help"/>

    <h:graphicImage id="required-image-mimetype"
                    value="/images/icons/required_field.gif" 
                    alt="#{msg.required_field}" 
                    rendered="#{WizardManager.bean.renderingEngineTemplateFileName != null}"/>
    <h:outputText id="not-required-mimetype" 
                  value=""
                  rendered="#{WizardManager.bean.renderingEngineTemplateFileName == null}"/>
    <h:outputText id="mimetype-output-text"
		  value="#{msg.mimetype_for_renditions}:"/>
    <h:selectOneMenu id="mimetype"
                     disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}"
		     valueChangeListener="#{WizardManager.bean.mimetypeForRenditionChanged}" 
		     value="#{WizardManager.bean.mimetypeForRendition}">
      <f:selectItems id="mimetype-choices"
		     value="#{WizardManager.bean.mimeTypeChoices}" />
    </h:selectOneMenu>
    <h:column id="mimetype-help"/>

    <h:graphicImage id="required-image-output-path-pattern"
                    value="/images/icons/required_field.gif"
                    rendered="#{WizardManager.bean.renderingEngineTemplateFileName != null}"
                    alt="#{msg.required_field}" />
    <h:outputText id="not-required-output-path-patern" 
                  value=""
                  rendered="#{WizardManager.bean.renderingEngineTemplateFileName == null}"/>
    <h:outputText id="output-path-pattern-output-text"
                  value="#{msg.output_path_pattern}:"/>
    <h:inputText id="output-path-pattern" 
                 disabled="#{WizardManager.bean.renderingEngineTemplateFileName == null}"
		 value="#{WizardManager.bean.outputPathPatternForRendition}"
		 style="width:100%;"/>
    <h:graphicImage id="graphic_image_output_path_pattern_help"
		    value="/images/icons/Help_icon.gif" style="cursor:help"
		    onclick="javascript:toggleOutputPathPatternHelp()" />

    <h:column id="output_path_pattern_help_empty_col_1"/>
    <h:column id="output_path_pattern_help_empty_col_2"/>
    <f:verbatim>
      <jsp:directive.include file="/jsp/wcm/output-path-pattern-help.jsp"/>
    </f:verbatim>
    <h:column id="output_path_pattern_help_empty_col_3"/>
  </h:panelGrid>

  <h:panelGroup id="step-3-panel-group" styleClass="mainSubText">
    <h:outputText id="step-3-output-text" value="3. " />
    <h:commandButton id="add-to-list-button" 
		     value="#{msg.add_to_list_button}" 
		     actionListener="#{WizardManager.bean.addSelectedRenderingEngineTemplate}" 
		     styleClass="wizardButton" 
		     disabled="#{WizardManager.bean.addToListDisabled}" />
  </h:panelGroup>
  <h:panelGroup id="data-table-panel-group">
    <h:dataTable id="rendering-engine-template-data-table"
                 value="#{WizardManager.bean.renderingEngineTemplatesDataModel}" var="row" 
                 rowClasses="selectedItemsRow,selectedItemsRowAlt"
                 styleClass="selectedItems" headerClass="selectedItemsHeader"
                 cellspacing="0" cellpadding="4" width="100%"
                 rendered="#{WizardManager.bean.renderingEngineTemplatesDataModel.rowCount != 0}">
      <h:column id="data-table-column-0">
        <f:facet name="header">
          <h:outputText id="data-table-name-0" value="#{msg.selected_rendering_engine_templates}" />
        </f:facet>
        <f:verbatim>
          <img style="float: left" src="<%= request.getContextPath() %>/images/icons/template_large.gif"/>
        </f:verbatim>
        <h:panelGrid id="panel_grid_row"
                     columns="2" cellspacing="1" border="0">
          <h:outputText id="data-table-name-0-name" value="#{msg.name}: " />
          <h:outputText id="data-table-value-0-name" value="#{row.name}" />

          <h:outputText id="data-table-name-0-type" value="#{msg.type}: " />
          <h:outputText id="data-table-value-0-type" value="#{row.renderingEngine.name}" />

          <h:outputText id="data-table-name-0-title" value="#{msg.title}: " />
          <h:outputText id="data-table-value-0-title" value="#{row.title}" />

          <h:outputText id="data-table-name-0-description" value="#{msg.description}: " />
          <h:outputText id="data-table-value-0-description-empty" style="font-style:italic"
                        rendered="#{empty row.description}" value="#{msg.description_not_set}" />
          <h:outputText id="data-table-value-0-description-not-empty" 
                        rendered="#{!empty row.description}" value="#{row.description}" />

          <h:outputText id="data-table-name-0-mimetype" value="#{msg.mimetype_for_renditions}: " />
          <h:outputText id="data-table-value-0-mimetype" value="#{row.mimetypeForRendition}" />

          <h:outputText id="data-table-name-0-opp" value="#{msg.output_path_pattern}: " />
          <h:outputText id="data-table-value-0-opp" value="#{row.outputPathPatternForRendition}" />
	</h:panelGrid>
      </h:column>
      <h:column id="data-table-column-5">
        <a:actionLink id="remove-select-rendering-engine-action-link"
		      actionListener="#{WizardManager.bean.removeSelectedRenderingEngineTemplate}" 
	              image="/images/icons/delete.gif" value="#{msg.remove}" showLink="false" style="padding:4px" />
      </h:column>
    </h:dataTable>
    
    <a:panel id="no-items" rendered="#{WizardManager.bean.renderingEngineTemplatesDataModel.rowCount == 0}">
      <h:panelGrid id="no-items-panel-grid" 
		   columns="1" 
		   cellpadding="2" 
		   styleClass="selectedItems" 
		   width="100%"
		   rowClasses="selectedItemsHeader,selectedItemsRow">
        <h:outputText id="no-items-name" value="#{msg.selected_rendering_engine_templates}" />
        <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
    </a:panel>
  </h:panelGroup>
</h:panelGrid>

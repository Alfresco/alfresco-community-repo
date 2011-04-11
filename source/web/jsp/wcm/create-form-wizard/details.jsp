<!--
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
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
  -->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
          xmlns:a="urn:jsptld:/WEB-INF/alfresco.tld"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page import="org.alfresco.web.ui.common.PanelGenerator"/>
  <jsp:directive.page import="org.alfresco.web.bean.wcm.CreateFormWizard"/>
  <jsp:directive.page language="java" buffer="32kb" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <f:verbatim>
    <script type="text/javascript" 
	    src="${pageContext.request.contextPath}/scripts/upload_helper.js">&#160;
    </script>
    
    <script type="text/javascript">
    var finishButtonPressed = false;
    addEventToElement(window, 'load', pageLoaded, false);
    
    function pageLoaded()
    {
       if (document.getElementById("wizard:wizard-body:file-input"))
         document.getElementById("wizard:wizard-body:file-input").focus()
       else
         document.getElementById("wizard:wizard-body:form-name").focus();
       document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
       document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
    }
    
    function validate()
    {
      if (!finishButtonPressed)
        return true;
      finishButtonPressed = false;
      var formName = document.getElementById("wizard:wizard-body:form-name");
      return validateMandatory(formName) <![CDATA[&&]]>
             validateName(formName, 
                          "${msg.validation_invalid_character}",
                          true)
    }
    
    function handle_upload(target)
    {
      handle_upload_helper(target, 
                           "<jsp:expression>CreateFormWizard.FILE_SCHEMA</jsp:expression>", 
                           upload_complete,
                           "${pageContext.request.contextPath}")
    }
 
    function upload_complete(id, path, filename)
    {
      var schema_file_input = 
        document.getElementById("wizard:wizard-body:schema-file");
      schema_file_input.value = filename;
      schema_file_input.form.submit();
    }
    
    function checkDisabledState()
    {
      var disabledElement = document.getElementById('wizard:next-button');
      var outputPathInput = document.getElementById('wizard:wizard-body:form_instance_data_output_path_pattern');
      var additionalConditionInput = document.getElementById('wizard:wizard-body:form-name');
      validateOutputPathPattern(disabledElement, outputPathInput, additionalConditionInput);
    }
    
    </script>
  </f:verbatim>

  <h:inputText id="schema-file" 
	       value="#{WizardManager.bean.schemaFileName}" 
	       immediate="true"
	       style="display:none;"
	       valueChangeListener="#{WizardManager.bean.schemaFileValueChanged}"/>
  <h:panelGrid rendered="#{!empty WizardManager.bean.associatedWebProjects}" 
               width="100%"
               styleClass="infoText">
    <h:outputText value="#{msg.create_form_form_details_associated_web_projects}"/>
    <f:verbatim>
      <ul>
        <c:forEach items="${WizardManager.bean.associatedWebProjects}" var="wp">
          <li>${wp.name}</li>
        </c:forEach>
      </ul>
    </f:verbatim>
  </h:panelGrid>

  <h:panelGrid id="panel_grid_1"
               columns="1" cellpadding="2" 
	       style="padding-top: 4px; padding-bottom: 4px;"
               width="100%" rowClasses="wizardSectionHeading">
    <h:outputText id="panel_grid_1_out_1"
                  value="&#160;#{msg.general_properties}" escape="false" />
  </h:panelGrid>

  <h:panelGrid id="panel_grid_2"
               columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
    <h:outputText id="step-1-text" 
		  value="1. #{msg.create_form_form_details_step1_desc}" 
		  escape="false" />
    <h:panelGrid id="schema_panel_grid"
                 columns="4" 
	         cellpadding="3" 
	         cellspacing="3" 
	         border="0" 
	         width="100%"
	         columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn,panelGridRequiredImageColumn">
      <h:graphicImage id="img_schema"
                      value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
      <h:outputText id="out_schema"
                    value="#{msg.schema}:"/>
      <h:column id="column_schema_empty"
	        rendered="#{empty WizardManager.bean.schemaFileName}">
        <f:verbatim><input id="wizard:wizard-body:file-input" 
			   type="file" 
			   size="35" 
			   name="alfFileInput" 
                           contentEditable="false" 
			   onchange="javascript:handle_upload(this)"/></f:verbatim>
      </h:column>
      <h:column id="column_schema_not_empty"
	        rendered="#{!empty WizardManager.bean.schemaFileName}">
        <h:outputText id="out_schema_name"
                      value="#{WizardManager.bean.schemaFileName}"/>
        <h:outputText id="out_schema_space"
                      value="&#160;"
		      escape="false"/>
        <a:actionLink id="action_link_remove_schema"
                      image="/images/icons/delete.gif" 
                      value="#{msg.remove}" 
                      action="#{WizardManager.bean.removeUploadedSchemaFile}"
                      showLink="false" 
		      target="top"/>
      </h:column>
      <h:column id="no_img_schema_help"/>
      
      <h:column id="no_img_root_element_name"/>
      <h:outputText id="out_root_element_name" value="#{msg.schema_root_element_name}:"/>
      <h:selectOneMenu id="schema-root-element-name" 
                       disabled="#{WizardManager.bean.schemaFileName == null}"
		       style="width:100%;"
                       value="#{WizardManager.bean.schemaRootElementName}"
		       rendered="#{!empty WizardManager.bean.schemaRootElementNameChoices}">
        <f:selectItems id="schema-root-element-name-choices"
		       value="#{WizardManager.bean.schemaRootElementNameChoices}"/>
      </h:selectOneMenu>
      <h:outputText id="schema-root-element-name-no-schema" 
		    rendered="#{empty WizardManager.bean.schemaRootElementNameChoices and empty WizardManager.bean.schemaFileName}"
		    value="#{msg.create_form_form_details_no_schema_selected}"/>
      <h:panelGroup id="schema-root-element-name-no-choices" 
                    rendered="#{empty WizardManager.bean.schemaRootElementNameChoices and !empty WizardManager.bean.schemaFileName}">
        <f:verbatim><jsp:scriptlet>PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");</jsp:scriptlet></f:verbatim>
        <h:panelGrid columns="2" cellspacing="0" cellpadding="0">
          <h:graphicImage url="/images/icons/warning.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
          <h:outputText styleClass="mainSubText" value="#{msg.create_form_form_details_no_elements_in_schema}"/>
        </h:panelGrid>
        <f:verbatim><jsp:scriptlet>PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");</jsp:scriptlet></f:verbatim>
      </h:panelGroup>
      <!-- we need to include this invisible image in order to get the column to size correctly -->
      <h:graphicImage id="invisible_img_schema_root_element_name_choices_help"
		      value="/images/icons/Help_icon.gif" style="cursor:help; visibility: hidden;"/>
    </h:panelGrid>

    <h:outputText id="step-2-text" value="2. #{msg.create_form_form_details_step2_desc}" escape="false" />
    
    <h:panelGrid id="details_panel_grid"
                 columns="4"
	         cellpadding="3" 
	         cellspacing="3" 
	         border="0" 
	         columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn,panelGridRequiredImageColumn"
                 width="100%">
      
      <h:graphicImage id="img_name" 
		      value="/images/icons/required_field.gif" 
		      alt="#{msg.required_field}" />
      <h:outputText id="out_name" value="#{msg.name}:"/>
      <h:inputText id="form-name" 
                   disabled="#{WizardManager.bean.schemaFileName == null}"
		             value="#{WizardManager.bean.formName}" onkeyup="javascript:checkDisabledState();"
                   maxlength="1024" size="35"/>
      <h:column id="no_img_name_help"/>
      
      <h:column id="no_img_title"/>
      <h:outputText id="out_title" value="#{msg.title}:"/>
      <h:inputText id="form-title" 
                   disabled="#{WizardManager.bean.schemaFileName == null}"
		   value="#{WizardManager.bean.formTitle}" 
                   maxlength="1024" 
		   size="35"/>
      <h:column id="no_img_title_help"/>
      
      <h:column id="no_img_description"/>
      <h:outputText id="out_description" value="#{msg.description}:"/>
      <h:inputText id="form-description" 
                   disabled="#{WizardManager.bean.schemaFileName == null}"
		   value="#{WizardManager.bean.formDescription}" 
                   maxlength="1024" 
		   style="width:100%"/>
      <h:outputText id="no_img_description_help" value=""/>
      
      <h:graphicImage id="img_form_instance_data_output_path_pattern" 
		      value="/images/icons/required_field.gif" 
		      alt="#{msg.required_field}"
		      rendered="#{WizardManager.bean.isWebForm == true}"/>
      <h:outputText id="out_form_instance_data_output_path_pattern" 
              value="#{msg.output_path_pattern}:"
		      rendered="#{WizardManager.bean.isWebForm == true}"/>
      <h:inputText id="form_instance_data_output_path_pattern" 
              disabled="#{WizardManager.bean.schemaFileName == null}"
		      value="#{WizardManager.bean.outputPathPatternForFormInstanceData}" 
		      style="width:100%"
              rendered="#{WizardManager.bean.isWebForm == true}"
              onkeyup="javascript:checkDisabledState();" />
      <h:graphicImage id="img_form_instance_data_output_path_pattern_help"
		      value="/images/icons/Help_icon.gif" style="cursor:help"
		      onclick="javascript:toggleOutputPathPatternHelp()" 
		      rendered="#{WizardManager.bean.isWebForm == true}"/>

      <h:column id="output_path_pattern_help_empty_col_1"/>
      <h:column id="output_path_pattern_help_empty_col_2"/>
      <f:verbatim>
        <c:import url="/jsp/wcm/output-path-pattern-help.jsp" />
      </f:verbatim>
      <h:column id="output_path_pattern_help_empty_col_3"/>

    </h:panelGrid>
  </h:panelGrid>
</jsp:root>

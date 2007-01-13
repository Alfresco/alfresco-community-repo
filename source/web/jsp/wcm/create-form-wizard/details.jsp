<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page import="org.alfresco.web.bean.wcm.CreateFormWizard" %>
<f:verbatim>
<script type="text/javascript" 
	src="<%=request.getContextPath()%>/scripts/validation.js">
</script>
<script type="text/javascript" 
	src="<%=request.getContextPath()%>/scripts/upload_helper.js">
</script>

<script type="text/javascript">
   var finishButtonPressed = false;
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      if (document.getElementById("wizard:wizard-body:file-input"))
        document.getElementById("wizard:wizard-body:file-input").focus()
      else
        document.getElementById("wizard:wizard-body:form-name").focus();
      document.getElementById("wizard").onsubmit = validate;
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
   }
   
   function validate()
   {
     if (!finishButtonPressed)
       return true;
     finishButtonPressed = false;
     var formName = document.getElementById("wizard:wizard-body:form-name");
     return validateMandatory(formName) &&
            validateName(formName, 
                         '</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" /><f:verbatim>',
                         true)
   }
   
   function handle_upload(target)
   {
     handle_upload_helper(target, 
                          "<%= CreateFormWizard.FILE_SCHEMA %>", 
                          upload_complete,
                          "<%= request.getContextPath() %>")
   }

   function upload_complete(id, path, filename)
   {
     var schema_file_input = 
       document.getElementById("wizard:wizard-body:schema-file");
     schema_file_input.value = filename;
     schema_file_input.form.submit();
   }
</script>
</f:verbatim>

<h:inputText id="schema-file" 
	     value="#{WizardManager.bean.schemaFileName}" 
	     immediate="true"
	     style="display:none;"
	     valueChangeListener="#{WizardManager.bean.schemaFileValueChanged}"/>

<h:panelGrid id="panel_grid_1"
             columns="1" cellpadding="2" 
	     style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText id="panel_grid_1_out_1"
                 value="&nbsp;#{msg.general_properties}" escape="false" />
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
			 onchange="javascript:handle_upload(this)"/></f:verbatim>
    </h:column>
    <h:column id="column_schema_not_empty"
	      rendered="#{!empty WizardManager.bean.schemaFileName}">
      <h:outputText id="out_schema_name"
                    value="#{WizardManager.bean.schemaFileName}"/>
      <h:outputText id="out_schema_space"
                    value="&nbsp;"
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
		     style="width:100%;"
                     value="#{WizardManager.bean.schemaRootElementName}"
		     rendered="#{!empty WizardManager.bean.schemaRootElementNameChoices}">
      <f:selectItems id="schema-root-element-name-choices"
		     value="#{WizardManager.bean.schemaRootElementNameChoices}"/>
    </h:selectOneMenu>
    <h:outputText id="schema-root-element-name-no-choices" 
		  rendered="#{empty WizardManager.bean.schemaRootElementNameChoices}"
		  value="#{msg.create_form_form_details_no_schema_selected}"/>
    <%-- we need to include this invisible image in order to get the column to size correctly --%>
    <h:graphicImage id="invisilbe_img_schema_root_element_name_choices_help"
		    value="/images/icons/Help_icon.gif" style="cursor:help; visibility: hidden;"/>
  </h:panelGrid>

  <h:outputText id="step-2-text" value="2. #{msg.create_form_form_details_step2_desc}" escape="false" />
  
  <h:panelGrid id="details_panel_grid"
               columns="4"
	       cellpadding="3" 
	       cellspacing="3" 
	       border="0" 
	       columnClasses="panelGridRequiredImageColumn,panelGridLabelColumn,panelGridValueColumn,panelGridRequiredImageColumn">
	       width="100%">
  
    <h:graphicImage id="img_name" 
		    value="/images/icons/required_field.gif" 
		    alt="#{msg.required_field}" />
    <h:outputText id="out_name" value="#{msg.name}:"/>
    <h:inputText id="form-name" 
		 value="#{WizardManager.bean.formName}"
                 maxlength="1024" 
		 size="35"/>
    <h:column id="no_img_name_help"/>
    
    <h:column id="no_img_title"/>
    <h:outputText id="out_title" value="#{msg.title}:"/>
    <h:inputText id="form-title" 
		 value="#{WizardManager.bean.formTitle}" 
                 maxlength="1024" 
		 size="35"/>
    <h:column id="no_img_title_help"/>
    
    <h:column id="no_img_description"/>
    <h:outputText id="out_description" value="#{msg.description}:"/>
    <h:inputText id="form-description" 
		 value="#{WizardManager.bean.formDescription}" 
                 maxlength="1024" 
		 style="width:100%"/>
    <h:outputText id="no_img_description_help" value=""/>
    
    <h:graphicImage id="img_form_instance_data_output_path_pattern" 
		    value="/images/icons/required_field.gif" 
		    alt="#{msg.required_field}" />
    <h:outputText id="out_form_instance_data_output_path_pattern" 
		  value="#{msg.output_path_pattern}:"/>
    <h:inputText id="form_instance_data_output_path_pattern" 
		 value="#{WizardManager.bean.outputPathPatternForFormInstanceData}" 
		 style="width:100%"/>
    <h:graphicImage id="img_form_instance_data_output_path_pattern_help"
		    value="/images/icons/Help_icon.gif" style="cursor:help"
		    onclick="javascript:toggleOutputPathPatternHelp()" />

    <h:column id="output_path_pattern_help_empty_col_1"/>
    <h:column id="output_path_pattern_help_empty_col_2"/>
    <f:verbatim>
      <jsp:directive.include file="/jsp/wcm/output-path-pattern-help.jsp"/>
    </f:verbatim>
    <h:column id="output_path_pattern_help_empty_col_3"/>

  </h:panelGrid>
</h:panelGrid>

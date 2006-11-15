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
<%@ page import="org.alfresco.web.bean.FileUploadBean" %>
<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js">
</script>

<script type="text/javascript">
   var finishButtonPressed = false;
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
//      document.getElementById("wizard:wizard-body:file-name").focus();
      document.getElementById("wizard").onsubmit = validate;
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
   }
   
   function validate()
   {
	return true;
   }

   function upload_file(el)
   {
     el.form.method = "post";
     el.form.encType = "multipart/form-data";
     // for IE
     el.form.encoding = "multipart/form-data";
     el.form.action = "<%= request.getContextPath() %>/uploadFileServlet";
     el.form.submit();
   }
</script>
</f:verbatim>

<h:panelGrid id="panel_grid_1"
             columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText id="panel_grid_1_output_text_1"
                 value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid id="panel_grid_2"
             columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <h:outputText id="step-1-text" 
		value="1. #{msg.create_form_form_details_step1_desc}" 
		escape="false" />
  
  <h:panelGrid id="schema_panel_grid"
               columns="3" cellpadding="3" cellspacing="3" border="0" width="100%">

    <h:graphicImage id="graphic_image_schema"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="output_text_schema"
                 value="#{msg.schema}:"/>
    <h:column id="column_schema">
<%
FileUploadBean upload = (FileUploadBean)
   session.getAttribute(FileUploadBean.getKey("schema"));
if (upload == null || upload.getFile() == null)
{
%>
      <f:verbatim>
        <input type="hidden" name="upload-id" value="schema"/>
        <input type="hidden" name="return-page" value="<%= request.getContextPath() %>/faces<%= request.getServletPath() %>"/>
        <input id="wizard:wizard-body:file-input" type="file" size="35" name="alfFileInput" onchange="javascript:upload_file(this)"/>
      </f:verbatim>
<%
} else {
%>
      <h:outputText id="output_text_schema_name"
                    value="#{WizardManager.bean.schemaFileName}"/>
      <h:outputText id="output_text_schema_space"
                    value="&nbsp;"
		    escape="false"/>
      <a:actionLink id="action_link_remove_schema"
                    image="/images/icons/delete.gif" 
                    value="#{msg.remove}" 
                    action="#{WizardManager.bean.removeUploadedSchemaFile}"
                    showLink="false" 
		    target="top"/>
<%
}
%>
    </h:column>
    
    <h:outputText id="no_graphic_image_root_element_name"
		  value=""/>
    <h:outputText id="output_text_root_element_name" value="#{msg.schema_root_element_name}:"/>
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

  </h:panelGrid>

  <h:outputText id="step-2-text" value="2. #{msg.create_form_form_details_step2_desc}" escape="false" />
  
  <h:panelGrid id="details_panel_grid"
               columns="3" cellpadding="3" cellspacing="3" border="0" width="100%">
  
    <h:graphicImage id="graphic_image_name" 
		    value="/images/icons/required_field.gif" 
		    alt="Required Field" />
    <h:outputText id="output_text_name" value="#{msg.name}:"/>
    <h:inputText id="file-name" 
		 value="#{WizardManager.bean.formName}" 
                 maxlength="1024" 
		 size="35"/>
    
    <h:outputText id="no_graphic_image_title" value=""/>
    <h:outputText id="output_text_title" value="#{msg.title}:"/>
    <h:inputText id="title" 
		 value="#{WizardManager.bean.formTitle}" 
                 maxlength="1024" 
		 size="35"/>
    
    <h:outputText id="no_graphic_image_description" value=""/>
    <h:outputText id="output_text_description" value="#{msg.description}:"/>
    <h:inputText id="description" 
		 value="#{WizardManager.bean.formDescription}" 
                 maxlength="1024" 
		 style="width:100%"/>
    
    <h:graphicImage id="graphic_image_form_instance_data_output_path_pattern" 
		    value="/images/icons/required_field.gif" 
		    alt="Required Field" />
    <h:outputText id="output_text_form_instance_data_output_path_pattern" 
		  value="#{msg.output_path_pattern}:"/>
    <h:inputText id="form_instance_data_output_path_pattern" 
		 value="#{WizardManager.bean.outputPathPatternForFormInstanceData}" 
		 style="width:100%"/>
  </h:panelGrid>
</h:panelGrid>

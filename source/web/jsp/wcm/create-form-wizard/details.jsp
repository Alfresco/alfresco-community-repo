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
      document.getElementById("wizard:wizard-body:file-name").focus();
      document.getElementById("wizard").onsubmit = validate;
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
   }
   
   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:file-input").value.length == 0 )
      {
         document.getElementById("wizard:next-button").disabled = true;
         document.getElementById("wizard:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
         document.getElementById("wizard:finish-button").disabled = false;
      }
   }
   
   function validate()
   {
//      if (finishButtonPressed)
//      {
//         finishButtonPressed = false;
//         return validateName(document.getElementById("wizard:wizard-body:file-name"), 
//                             '</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" /><f:verbatim>',
//                             true);
//      }
//      else
//      {
//         return true;
//      }
	return true;
   }

   function upload_file(el)
   {
     el.form.method = "post";
     el.form.enctype = "multipart/form-data";
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
             columns="3" cellpadding="3" cellspacing="3" border="0">
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
   <h:graphicImage id="graphic_image_root_tag_name"
		   value="/images/icons/required_field.gif" alt="Required Field" />
   <h:outputText id="output_text_root_tag_name" value="#{msg.schema_root_tag_name}:"/>
   <h:inputText id="schema-root-tag-name" value="#{WizardManager.bean.schemaRootTagName}" 
                maxlength="1024" size="35"/>
   <h:graphicImage id="graphic_image_name" value="/images/icons/required_field.gif" alt="Required Field" />
   <h:outputText id="output_text_name" value="#{msg.name}:"/>
   <h:inputText id="file-name" value="#{WizardManager.bean.templateName}" 
                maxlength="1024" size="35"/>
</h:panelGrid>

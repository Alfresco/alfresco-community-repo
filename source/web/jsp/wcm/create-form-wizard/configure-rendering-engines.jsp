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

<jsp:directive.page import="java.io.*"/>
<jsp:directive.page import="org.alfresco.web.bean.FileUploadBean"/>
<jsp:directive.page import="org.alfresco.web.bean.wcm.CreateFormWizard"/>
<jsp:directive.page buffer="32kb" contentType="text/html;charset=UTF-8"/>
<jsp:directive.page isELIgnored="false"/>

<f:verbatim>
  <script type="text/javascript">
    function upload_file(el)
    {
       el.form.method = "post";
       el.form.enctype = "multipart/form-data";
       // for IE
       el.form.encoding = "multipart/form-data";
       el.form.action = "<%= request.getContextPath() %>/uploadFileServlet";
       el.form.submit();
       return false;
    }
  </script>
</f:verbatim>

<h:panelGrid id="general-properties-panel-grid" 
	     columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%">
  <h:outputText id="step-1-text" 
		value="1. #{msg.create_form_configure_rendering_engine_templates_step1_desc}" 
		escape="false" />
  <h:panelGrid id="panel_grid_3"
               columns="3" cellpadding="3" cellspacing="3" border="0"
               width="100%">
    <h:graphicImage id="required_image_rendering_engine_template_file"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="output_text_rendering_engine_template_file"
                  value="#{msg.rendering_engine_template_file}:"/>
    <h:column id="column_pt">
<%
final FileUploadBean upload = (FileUploadBean)
   session.getAttribute(FileUploadBean.getKey(CreateFormWizard.FILE_RENDERING_ENGINE_TEMPLATE));
if (upload == null || upload.getFile() == null)
{
%>

      <f:verbatim>
	<input type="hidden" name="upload-id" value="<%= CreateFormWizard.FILE_RENDERING_ENGINE_TEMPLATE %>"/>
	<input type="hidden" name="return-page" value="<%= request.getContextPath() %>/faces<%= request.getServletPath() %>"/>
	<input id="wizard:wizard-body:file-input" type="file" size="35" name="alfFileInput" onchange="javascript:upload_file(this)"/>
      </f:verbatim>
<%
} 
else 
{
%>
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
<%
}
%>
    </h:column>
  </h:panelGrid>

  <h:outputText id="step-2-text" 
		value="2. #{msg.create_form_configure_rendering_engine_templates_step2_desc}" 
		escape="false" />
  <h:panelGrid id="panel_grid_specify_details"
               columns="3" cellpadding="3" cellspacing="3" border="0"
               width="100%">

    <h:graphicImage id="required-image-rendering-engine"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="rendering-engine-output-text"
                  value="#{msg.rendering_engine}:"/>
    <h:selectOneRadio id="rendering-engine" 
		      value="#{WizardManager.bean.renderingEngineName}">
     <f:selectItems id="rendering-engine-choices"
		    value="#{WizardManager.bean.renderingEngineChoices}"/>
    </h:selectOneRadio>

    <h:graphicImage id="required-image-mimetype"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="mimetype-output-text"
		  value="#{msg.mimetype_for_renditions}:"/>
    <h:selectOneMenu id="mimetype"
		     valueChangeListener="#{WizardManager.bean.mimetypeForRenditionChanged}" 
		     value="#{WizardManager.bean.mimetypeForRendition}">
      <f:selectItems id="mimetype-choices"
		     value="#{WizardManager.bean.mimeTypeChoices}" />
    </h:selectOneMenu>

    <h:graphicImage id="required-image-output-path-pattern"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="output-path-pattern-output-text"
                  value="#{msg.output_path_pattern}:"/>
    <h:inputText id="output-path-pattern" 
		 value="#{WizardManager.bean.outputPathPatternForRendition}"
		 style="width:100%;"/>
  </h:panelGrid>

  <h:panelGroup id="step-3-panel-group" styleClass="mainSubText">
    <h:outputText id="step-3-output-text" value="3. " />
    <h:commandButton id="add-to-list-button" 
		     value="#{msg.add_to_list_button}" 
		     actionListener="#{WizardManager.bean.addSelectedRenderingEngineTemplate}" 
		     styleClass="wizardButton" 
		     disabled="#{WizardManager.bean.addToListDisabled}" />
  </h:panelGroup>
  <h:outputText id="selected-rendering-engine-templates-output-text"
                styleClass="mainSubText" 
		value="#{msg.selected_rendering_engine_templates}" />
  <h:panelGroup id="data-table-panel-group">
    <h:dataTable id="rendering-engine-template-data-table"
                 value="#{WizardManager.bean.renderingEngineTemplatesDataModel}" 
		 var="row" 
                 rowClasses="selectedItemsRow,selectedItemsRowAlt"
                 styleClass="selectedItems" 
		 headerClass="selectedItemsHeader"
                 cellspacing="0" 
		 cellpadding="4" 
                 rendered="#{WizardManager.bean.renderingEngineTemplatesDataModel.rowCount != 0}">
      <h:column id="data-table-column-1">
        <f:facet name="header">
          <h:outputText id="data-table-name-1" value="#{msg.file_name}" />
        </f:facet>
        <h:outputText id="data-table-value-1" value="#{row.fileName}" />
      </h:column>
      <h:column id="data-table-column-2">
        <f:facet name="header">
          <h:outputText id="data-table-name-2" value="#{msg.type}" />
        </f:facet>
        <h:outputText id="data-table-value-2" value="#{row.renderingEngine.name}" />
      </h:column>
      <h:column id="data-table-column-3">
        <f:facet name="header">
          <h:outputText id="data-table-name-3" value="#{msg.output_path_pattern}" />
        </f:facet>
        <h:outputText id="data-table-value-3" value="#{row.outputPathPatternForRendition}" />
      </h:column>
      <h:column id="data-table-column-4">
        <f:facet name="header">
          <h:outputText id="data-table-name-4" value="#{msg.mimetype}" />
        </f:facet>
        <h:outputText id="data-table-value-4" value="#{row.mimetypeForRendition}" />
      </h:column>
      <h:column id="data-table-column-5">
        <a:actionLink id="remove-select-rendering-engine-action-link"
		      actionListener="#{WizardManager.bean.removeSelectedRenderingEngineTemplate}" 
	              image="/images/icons/delete.gif"
                      value="#{msg.remove}" showLink="false" style="padding-left:6px" />
      </h:column>
    </h:dataTable>
    
    <a:panel id="no-items" rendered="#{WizardManager.bean.renderingEngineTemplatesDataModel.rowCount == 0}">
      <h:panelGrid id="no-items-panel-grid" 
		   columns="1" 
		   cellpadding="2" 
		   styleClass="selectedItems" 
		   rowClasses="selectedItemsHeader,selectedItemsRow">
        <h:outputText id="no-items-name" value="#{msg.name}" />
        <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
    </a:panel>
  </h:panelGroup>
</h:panelGrid>

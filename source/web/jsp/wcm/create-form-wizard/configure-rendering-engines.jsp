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
  <h:outputText id="step-1-text" value="1. #{msg.configure_rendering_engines_step1_desc}" escape="false" />
  <h:panelGrid id="panel_grid_3"
               columns="3" cellpadding="3" cellspacing="3" border="0"
               width="100%">
    <h:graphicImage id="required_image_pt"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="output_text_pt"
                  value="#{msg.rendering_engine_file}:"/>
<%
final FileUploadBean upload = (FileUploadBean)
   session.getAttribute(FileUploadBean.getKey(CreateFormWizard.FILE_RENDERING_ENGINE));
if (upload == null || upload.getFile() == null)
{
%>
    <h:column id="column_pt">
      <f:verbatim>
	<input type="hidden" name="upload-id" value="<%= CreateFormWizard.FILE_RENDERING_ENGINE %>"/>
	<input type="hidden" name="return-page" value="<%= request.getContextPath() %>/faces<%= request.getServletPath() %>"/>
	<input id="wizard:wizard-body:file-input" type="file" size="35" name="alfFileInput" onchange="javascript:upload_file(this)"/>
      </f:verbatim>
    </h:column>
<%
} 
else 
{
%>
    <h:outputText id="rendering-engine-file-name"
                  value="#{WizardManager.bean.renderingEngineFileName}"/>
<%
}
%>
    <h:graphicImage id="required-image-rendering-engine-type"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="rendering-engine-type-output-text"
                  value="#{msg.type}:"/>
    <h:selectOneRadio id="rendering-engine-type" 
		      value="#{WizardManager.bean.renderingEngineType}">
     <f:selectItems id="rendering-engine-type-choices"
		    value="#{WizardManager.bean.renderingEngineTypeChoices}"/>
    </h:selectOneRadio>

    <h:graphicImage id="required-image-file-extension"
                    value="/images/icons/required_field.gif" alt="Required Field" />
    <h:outputText id="file-extension-output-text"
                  value="#{msg.extension_for_generated_assets}:"/>
    <h:inputText id="file-extension" value="#{WizardManager.bean.fileExtension}"
                 maxlength="10" size="10"/>
  </h:panelGrid>

  <h:panelGroup id="step-2-panel-group" styleClass="mainSubText">
    <h:outputText id="step-2-output-text" value="2." />
    <h:commandButton id="add-to-list-button" value="#{msg.add_to_list_button}" 
		     actionListener="#{WizardManager.bean.addSelectedRenderingEngine}" 
		     styleClass="wizardButton" disabled="#{WizardManager.bean.addToListDisabled}" />
  </h:panelGroup>
  <h:outputText id="selected-rendering-engines-output-text"
                styleClass="mainSubText" 
		value="#{msg.selected_rendering_engines}" />
  <h:panelGroup id="data-table-panel-group">
    <h:dataTable id="rendering-engine-data-table"
                 value="#{WizardManager.bean.renderingEnginesDataModel}" 
		 var="row" 
                 rowClasses="selectedItemsRow,selectedItemsRowAlt"
                 styleClass="selectedItems" 
		 headerClass="selectedItemsHeader"
                 cellspacing="0" 
		 cellpadding="4" 
                 rendered="#{WizardManager.bean.renderingEnginesDataModel.rowCount != 0}">
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
        <h:outputText id="data-table-value-2" value="#{row.renderingEngineTypeName}" />
      </h:column>
      <h:column id="data-table-column-3">
        <f:facet name="header">
          <h:outputText id="data-table-name-3" value="#{msg.file_extension}" />
        </f:facet>
        <h:outputText id="data-table-value-3" value="#{row.fileExtension}" />
      </h:column>
      <h:column id="data-table-column-4">
        <a:actionLink id="remove-select-rendering-engine-action-link"
		      actionListener="#{WizardManager.bean.removeSelectedRenderingEngine}" 
	              image="/images/icons/delete.gif"
                      value="#{msg.remove}" showLink="false" style="padding-left:6px" />
      </h:column>
    </h:dataTable>
    
    <a:panel id="no-items" rendered="#{WizardManager.bean.renderingEnginesDataModel.rowCount == 0}">
      <h:panelGrid id="no-items-panel-grid" 
		   columns="1" 
		   cellpadding="2" 
		   styleClass="selectedItems" 
		   rowClasses="selectedItemsHeader,selectedItemsRow">
        <h:outputText styleClass="selectedItemsHeader" id="no-items-name" value="#{msg.name}" />
        <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
    </a:panel>
  </h:panelGroup>
</h:panelGrid>

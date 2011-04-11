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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page isELIgnored="false" %>

  <script type="text/javascript">
    addEventToElement(window, 'load', pageLoaded, false);
    function pageLoaded() { document.getElementById("wizard:finish-button").focus(); }
  </script>

  <h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
               width="100%" rowClasses="wizardSectionHeading">
    <h:outputText value="&#160;#{msg.create_web_content_summary_content_details}" escape="false" />
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
    <a:selectList id="form-instance-data-list" 
                  multiSelect="false"
                  activeSelect="true" 
                  style="width:100%" 
                  itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;"
                  escapeItemLabel="false" escapeItemDescription="false">
      <a:listItem value="${WizardManager.bean.formInstanceData.name}"
                  image="/images/filetypes32/xml.gif"
                  label="${WizardManager.bean.formLabelAttribute}"
                  description="${WizardManager.bean.formDescriptionAttribute}" />
    </a:selectList>
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
               width="100%" rowClasses="wizardSectionHeading"
               rendered="#{!empty WizardManager.bean.renditions}">
    <h:outputText value="&#160;#{msg.create_web_content_summary_rendition_details}" escape="false" />
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%"
	       rendered="#{!empty WizardManager.bean.renditions}">
    <a:selectList id="rendition-list" 
                  multiSelect="false"
                  activeSelect="true" 
                  style="width:100%" 
                  itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;"
                  escapeItemLabel="false" escapeItemDescription="false">
      <c:forEach items="${WizardManager.bean.renditions}" var="rendition" varStatus="status">
        <a:listItem id="listItem${status.index}" value="${rendition.name}" image="${rendition.fileTypeImage}"
        			label="${rendition.labelAttribute}"
        			description="${rendition.descriptionAttribute}" /> 
      </c:forEach>
    </a:selectList>
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
               width="100%" rowClasses="wizardSectionHeading"
	       rendered="#{!empty WizardManager.bean.uploadedFiles}">
    <h:outputText value="&#160;#{msg.create_web_content_summary_uploaded_files_details}" escape="false" />
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%"
	       rendered="#{!empty WizardManager.bean.uploadedFiles}">
    <a:selectList id="uploaded-file-list" 
		  multiSelect="false"
		  activeSelect="true" 
		  style="width:100%" 
		  itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;">
      <a:listItems value="#{WizardManager.bean.uploadedFiles}"/>
    </a:selectList>
  </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
               width="100%"
               rendered="#{WizardManager.bean.submittable}">
    <h:column>
      <h:selectBooleanCheckbox id="startWorkflow"
			       value="#{WizardManager.bean.startWorkflow}"/>
      <h:outputFormat value="&#160;#{msg.create_web_content_summary_submit_message}" escape="false">
        <f:param value="#{WizardManager.bean.numberOfSubmittableFiles}"/>
        <f:param value="#{WizardManager.bean.formInstanceData.name}"/>
      </h:outputFormat>
    </h:column>
  </h:panelGrid>

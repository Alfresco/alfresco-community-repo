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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page isELIgnored="false" %>

<script type="text/javascript">
  window.onload = function() { document.getElementById("wizard:finish-button").focus(); }
</script>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <a:selectList id="form-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%"
		itemStyle="vertical-align: top; margin-right: 5px;">
    <a:listItem label="<b>${WizardManager.bean.formTitle}</b>"
                value="${WizardManager.bean.formName}"
                image="/images/icons/webform_large.gif">
      <jsp:attribute name="description">
	<table width="100%" cellspacing="0" cellpadding="0" border="0">
	  <colgroup><col width="25%"/><col width="75%"/></colgroup>
	  <tbody>
            <tr><td>${msg.description}:</td>
	      <td>
		<c:choose>
		  <c:when test="${empty WizardManager.bean.formDescription}">
		    <span style="font-style:italic">${msg.description_not_set}</span>
		  </c:when>
		  <c:otherwise>${WizardManager.bean.formDescription}</c:otherwise>
		</c:choose>
	      </td>
	    </tr>
            <tr><td>${msg.schema_root_element_name}:</td><td>${WizardManager.bean.schemaRootElementName}</td></tr>
            <tr><td>${msg.output_path_pattern}:</td><td>${WizardManager.bean.outputPathPatternForFormInstanceData}</td></tr>
	  </tbody>
	</table>
      </jsp:attribute>
    </a:listItem>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.rendering_engine_templates}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <h:outputText rendered="#{empty WizardManager.bean.renderingEngineTemplates}"
		value="#{msg.no_selected_items}"/>

  <a:selectList id="rendering-engine-template-list" 
                multiSelect="false"
		itemStyle="vertical-align: top; margin-right: 5px;"
                activeSelect="true" 
                style="width:100%">
    <c:forEach items="${WizardManager.bean.renderingEngineTemplates}" var="ret">
      <a:listItem label="<b>${ret.title}</b>"
                  value="${ret.name}"
                  image="/images/icons/template_large.gif">
        <jsp:attribute name="description">
	  <table width="100%" cellspacing="0" cellpadding="0" border="0">
	    <colgroup><col width="25%"/><col width="75%"/></colgroup>
	    <tbody>
              <tr><td>${msg.description}:</td>
		<td>
		  <c:choose>
		    <c:when test="${empty ret.description}">
		      <span style="font-style:italic">${msg.description_not_set}</span>
		    </c:when>
		    <c:otherwise>${ret.description}</c:otherwise>
		  </c:choose>
		</td>
	      </tr>
              <tr><td>${msg.rendering_engine_type}:</td><td>${ret.renderingEngine.name}</td></tr>
              <tr><td>${msg.output_path_pattern}:</td><td>${ret.outputPathPatternForRendition}</td></tr>
              <tr><td>${msg.mimetype_for_renditions}:</td><td>${ret.mimetypeForRendition}</td></tr>
	    </tbody>
	  </table>
        </jsp:attribute>
      </a:listItem>
    </c:forEach>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.default_workflow}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <h:outputText value="#{msg.no_selected_items}" 
		rendered="#{WizardManager.bean.defaultWorkflowDefinition == null}"/>
  <a:selectList id="workflow-list" 
		rendered="#{WizardManager.bean.defaultWorkflowDefinition != null}"
                multiSelect="false"
                activeSelect="true" 
		itemStyle="vertical-align: top; margin-right: 5px;"
                style="width:100%">
    <a:listItem label="<b>${WizardManager.bean.defaultWorkflowDefinition.title}</b>"
                value="${WizardManager.bean.defaultWorkflowDefinition.name}"
                image="/images/icons/workflow_large.gif">
      <jsp:attribute name="description">
	<table width="100%" cellspacing="0" cellpadding="0" border="0">
	  <colgroup><col width="25%"/><col width="75%"/></colgroup>
	  <tbody>
            <tr><td>${msg.description}:</td>
	      <td>
		<c:choose>
		  <c:when test="${empty WizardManager.bean.defaultWorkflowDefinition.description}">
		    <span style="font-style:italic">${msg.description_not_set}</span>
		  </c:when>
		  <c:otherwise>${WizardManager.bean.defaultWorkflowDefinition.description}</c:otherwise>
		</c:choose>
	      </td>
	    </tr>
	  </tbody>
	</table>
      </jsp:attribute>
    </a:listItem>
  </a:selectList>
</h:panelGrid>

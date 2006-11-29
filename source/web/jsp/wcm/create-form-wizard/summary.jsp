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

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
  <h:outputText value="#{msg.name}:"/>
  <h:outputText value="#{WizardManager.bean.formName}"/>
  <h:outputText value="#{msg.title}:"/>
  <h:outputText value="#{WizardManager.bean.formTitle}"/>
  <h:outputText value="#{msg.description}:"/>
  <h:outputText value="#{WizardManager.bean.formDescription}"/>
  <h:outputText value="#{msg.schema_root_element_name}:"/>
  <h:outputText value="#{WizardManager.bean.schemaRootElementName}"/>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.rendering_engine_templates}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
  <c:forEach items="${WizardManager.bean.renderingEngineTemplates}" var="ret">
    <h:outputText value="#{msg.name}:"/>
    <f:verbatim>${ret.fileName}</f:verbatim>
<%--
    <h:outputText value="#{msg.title}:"/>
    <f:verbatim>${ret.title}</f:verbatim>
    <h:outputText value="#{msg.description}:"/>
    <f:verbatim>${ret.description}</f:verbatim>
--%>
    <h:outputText value="#{msg.rendering_engine_type}:"/>
    <f:verbatim>${ret.renderingEngine.name}</f:verbatim>
    <h:outputText value="#{msg.output_path_pattern}:"/>
    <f:verbatim>${ret.outputPathPatternForRendition}</f:verbatim>
    <h:outputText value="#{msg.mimetype_for_renditions}:"/>
    <f:verbatim>${ret.mimetypeForRendition}</f:verbatim>
  </c:forEach>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.default_workflow}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
  <h:outputText value="#{msg.apply_default_workflow}:"/>
  <c:choose>
    <c:when test="${WizardManager.bean.defaultWorkflowName != null}">
      <h:outputText value="#{msg.yes}"/>
    </c:when>
    <c:otherwise>
      <h:outputText value="#{msg.no}"/>
    </c:otherwise>
  </c:choose>
  <c:if test="${WizardManager.bean.defaultWorkflowName != null}">
    <h:outputText value="#{msg.name}:"/>
    <h:outputText value="#{WizardManager.bean.defaultWorkflowDefinition.name}"/>
    <h:outputText value="#{msg.description}:"/>
    <h:outputText value="#{WizardManager.bean.defaultWorkflowDefinition.description}"/>
  </c:if>
</h:panelGrid>

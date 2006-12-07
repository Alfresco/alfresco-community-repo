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
  <a:selectList id="form-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%" 
                itemStyleClass="selectListItem">
    <a:listItem label="${WizardManager.bean.formTitle}"
                value="${WizardManager.bean.formName}"
                image="/images/icons/webform_large.gif">
      <jsp:attribute name="description">
        <div>${WizardManager.bean.formDescription}</div>
        <div>${msg.schema_root_element_name}: ${WizardManager.bean.schemaRootElementName}</div>
        <div>${msg.schema_root_element_name}: ${WizardManager.bean.outputPathPatternForFormInstanceData}</div>
      </jsp:attribute>
    </a:listItem>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.rendering_engine_templates}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0">
  <a:selectList id="rendering-engine-template-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%" 
                itemStyleClass="selectListItem">
    <c:forEach items="${WizardManager.bean.renderingEngineTemplates}" var="ret">
      <a:listItem label="${ret.title}"
                  value="${ret.fileName}"
                  image="/images/icons/template_large.gif">
        <jsp:attribute name="description">
          <div>${msg.description}: ${ret.description}</div>
          <div>${msg.rendering_engine_type}: ${ret.renderingEngine.name}</div>
          <div>${msg.output_path_pattern}: ${ret.outputPathPatternForRendition}</div>
          <div>${msg.mimetype_for_renditions}: ${ret.mimetypeForRendition}</div>
        </jsp:attribute>
      </a:listItem>
    </c:forEach>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
  <h:outputText value="&nbsp;#{msg.default_workflow}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
  <h:outputText value="#{msg.apply_default_workflow}:"/>
  <c:choose>
    <c:when test="${WizardManager.bean.defaultWorkflowDefinition != null}">
      <h:outputText value="#{msg.yes}"/>
    </c:when>
    <c:otherwise>
      <h:outputText value="#{msg.no}"/>
    </c:otherwise>
  </c:choose>
  <c:if test="${WizardManager.bean.defaultWorkflowDefinition != null}">
    <h:outputText value="#{msg.name}:"/>
    <h:outputText value="#{WizardManager.bean.defaultWorkflowDefinition.title}"/>
    <h:outputText value="#{msg.description}:"/>
    <h:outputText value="#{WizardManager.bean.defaultWorkflowDefinition.description}"/>
  </c:if>
</h:panelGrid>

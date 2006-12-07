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

<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" style="margin-left:16px" styleClass="summary">
   <h:outputText value="#{msg.name}:" styleClass="mainSubTitle" />
   <h:outputText value="#{WizardManager.bean.name}"/>
   <h:outputText value="#{msg.website_dnsname}:" styleClass="mainSubTitle" />
   <h:outputText value="#{WizardManager.bean.dnsName}"/>
   <h:outputText value="#{msg.website_webapp}:" styleClass="mainSubTitle" />
   <h:outputText value="#{WizardManager.bean.webapp}"/>
   <h:outputText value="#{msg.title}:" styleClass="mainSubTitle" />
   <h:outputText value="#{WizardManager.bean.title}"/>
   <h:outputText value="#{msg.description}:" styleClass="mainSubTitle" />
   <h:outputText value="#{WizardManager.bean.description}"/>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.website_web_content_forms}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" style="margin-left:16px" styleClass="summary">
   <c:forEach items="${WizardManager.bean.forms}" var="r">
      <h:outputText value="#{msg.name}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.name}</f:verbatim>
      <h:outputText value="#{msg.title}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.title}</f:verbatim>
      <h:outputText value="#{msg.website_filename_pattern}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.filenamePattern}</f:verbatim>
      <h:outputText value="#{msg.workflow}:" styleClass="mainSubTitle" />
      <c:if test="${r.workflow != null}">
         <f:verbatim>${r.workflow.name}</f:verbatim>
      </c:if>
      <c:if test="${r.workflow == null}">
         <f:verbatim>${msg.none}</f:verbatim>
      </c:if>
      <f:verbatim />
      <f:verbatim />
   </c:forEach>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.website_selected_workflows}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" style="margin-left:16px" styleClass="summary">
   <c:forEach items="${WizardManager.bean.workflows}" var="r">
      <h:outputText value="#{msg.name}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.title}</f:verbatim>
      <h:outputText value="#{msg.description}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.description}</f:verbatim>
      <h:outputText value="#{msg.website_filename_pattern}:" styleClass="mainSubTitle" />
      <f:verbatim>${r.filenamePattern}</f:verbatim>
      <f:verbatim />
      <f:verbatim />
   </c:forEach>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.create_website_summary_users}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" style="margin-left:12px">
   <h:outputText value="#{WizardManager.bean.summary}" escape="false" />
</h:panelGrid>

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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   
   window.onload = pageLoaded;
   var okEnabled;
   
   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:subject").focus();
      okEnabled = !document.getElementById("wizard:finish-button").disabled;
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (okEnabled)
      {
         if (document.getElementById("wizard:wizard-body:subject").value.length == 0)
         {
            document.getElementById("wizard:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("wizard:finish-button").disabled = false;
         }
      }
   }
</script>
</f:verbatim>

<h:panelGrid style="padding-bottom:12px" columns="1" cellpadding="2" cellspacing="2" border="0" width="100%">
   <h:outputText value="#{msg.send_email}" />
   <h:selectOneRadio value="#{WizardManager.bean.notify}">
      <f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
      <f:selectItem itemValue="no" itemLabel="#{msg.no}" />
   </h:selectOneRadio>
</h:panelGrid>

<h:outputText value="#{msg.email_message}" styleClass="mainSubTitle" />
<h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0" width="100%">
   <h:outputText value="#{msg.subject}:" />
   <h:panelGroup>
      <h:inputText id="subject" value="#{WizardManager.bean.mailHelper.subject}" size="75" maxlength="1024" onkeyup="javascript:checkButtonState();" />
      <f:verbatim>&nbsp;*</f:verbatim>
   </h:panelGroup>
   
   <f:verbatim></f:verbatim>
   <h:panelGrid columns="4" cellspacing="1" cellpadding="1" border="0">
      <h:outputText value="#{msg.action_mail_template}:" />
      <h:selectOneMenu value="#{WizardManager.bean.mailHelper.template}">
         <f:selectItems value="#{TemplateSupportBean.emailTemplates}" />
      </h:selectOneMenu>
      <h:commandButton value="#{msg.insert_template}" actionListener="#{WizardManager.bean.mailHelper.insertTemplate}" styleClass="wizardButton" />
      <h:commandButton value="#{msg.discard_template}" actionListener="#{WizardManager.bean.mailHelper.discardTemplate}" styleClass="wizardButton" disabled="#{WizardManager.bean.mailHelper.usingTemplate == null}" />
   </h:panelGrid>
   
   <h:outputText value="#{msg.message}:"/>
   <h:inputTextarea value="#{WizardManager.bean.mailHelper.body}" rows="4" cols="75" disabled="#{WizardManager.bean.mailHelper.usingTemplate != null}" />
</h:panelGrid>

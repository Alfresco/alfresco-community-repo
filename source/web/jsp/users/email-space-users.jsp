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

<f:verbatim>
<script type="text/javascript">
   
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:subject").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:subject").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>
</f:verbatim>

<h:outputText styleClass="mainSubTitle" value="#{msg.message_recipients}" />
<f:verbatim><div style='padding:2px'></div></f:verbatim>
<r:userGroupPicker value="#{DialogManager.bean.usersGroups}" actionListener="#{DialogManager.bean.userGroupSelectorAction}" />

<f:verbatim><div style='padding:8px'></div></f:verbatim>

<h:outputText styleClass="mainSubTitle" value="#{msg.email_message}" />
<h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0" width="100%">
   <h:outputText value="#{msg.subject}:" />
   <h:panelGroup>
      <h:inputText id="subject" value="#{DialogManager.bean.mailHelper.subject}" size="75" maxlength="1024" onkeyup="javascript:checkButtonState();" />
      <f:verbatim>&nbsp;*</f:verbatim>
   </h:panelGroup>
   
   <f:verbatim></f:verbatim>
   <h:panelGrid columns="4" cellspacing="1" cellpadding="1" border="0">
      <h:outputText value="#{msg.action_mail_template}:" />
      <h:selectOneMenu value="#{DialogManager.bean.mailHelper.template}">
         <f:selectItems value="#{TemplateSupportBean.emailTemplates}" />
      </h:selectOneMenu>
      <h:commandButton value="#{msg.insert_template}" actionListener="#{DialogManager.bean.mailHelper.insertTemplate}" styleClass="wizardButton" />
      <h:commandButton value="#{msg.discard_template}" actionListener="#{DialogManager.bean.mailHelper.discardTemplate}" styleClass="wizardButton" disabled="#{DialogManager.bean.mailHelper.usingTemplate == null}" />
   </h:panelGrid>
   
   <h:outputText value="#{msg.message}:"/>
   <h:inputTextarea value="#{DialogManager.bean.mailHelper.body}" rows="4" cols="75" disabled="#{DialogManager.bean.mailHelper.usingTemplate != null}" />
</h:panelGrid>

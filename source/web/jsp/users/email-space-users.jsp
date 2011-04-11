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

<f:verbatim>
<script type="text/javascript">
   
   addEventToElement(window, 'load', pageLoaded, false);
   var okEnabled;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:subject").focus();
      okEnabled = !document.getElementById("dialog:finish-button").disabled;
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (okEnabled)
      {
         if (document.getElementById("dialog:dialog-body:subject").value.trim().length == 0 ||
            document.getElementById("dialog:dialog-body:message").value.trim().length == 0)
         {
            document.getElementById("dialog:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("dialog:finish-button").disabled = false;
         }
      }
   }
</script>
</f:verbatim>

<a:panel id="no-message-recipients-panel" rendered="#{empty DialogManager.bean.usersGroups}">

<h:outputText value="#{msg.email_space_users_no_recipients}" />

</a:panel>

<a:panel id="message-recipients-panel" rendered="#{not empty DialogManager.bean.usersGroups}">

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
         <f:selectItems value="#{TemplateSupportBean.notifyEmailTemplates}" />
      </h:selectOneMenu>
      <h:commandButton value="#{msg.insert_template}" actionListener="#{DialogManager.bean.mailHelper.insertTemplate}" styleClass="wizardButton" />
      <h:commandButton value="#{msg.discard_template}" actionListener="#{DialogManager.bean.mailHelper.discardTemplate}" styleClass="wizardButton" disabled="#{DialogManager.bean.mailHelper.usingTemplate == null}" />
   </h:panelGrid>
   
   <h:outputText value="#{msg.message}:"/>
   <h:inputTextarea id="message" value="#{DialogManager.bean.mailHelper.body}" rows="4" cols="75" disabled="#{DialogManager.bean.mailHelper.usingTemplate != null}" onkeyup="javascript:checkButtonState();" />
</h:panelGrid>

</a:panel>
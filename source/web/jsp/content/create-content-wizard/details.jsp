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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
   var finishButtonPressed = false;
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:name").focus();
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
   }
   
   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:name").value.length == 0 )
      {
         document.getElementById("wizard:next-button").disabled = true;
         //document.getElementById("wizard:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
         //document.getElementById("wizard:finish-button").disabled = false;
      }
   }
   
   function formChanged()
   {
      if (document.getElementById("wizard:wizard-body:form-name").value != "")
      {
         document.getElementById("wizard:wizard-body:mime-type").disabled = true;
         document.getElementById("wizard:wizard-body:mime-type").value = "text/xml";
      }
      else
      {
         document.getElementById("wizard:wizard-body:mime-type").disabled = false;
      }
   }

</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.name}:"/>
   <h:inputText id="name" value="#{WizardManager.bean.fileName}" 
                maxlength="1024" size="35" 
                onkeyup="checkButtonState();" 
                onchange="checkButtonState();" />
                
   <h:outputText value=""/>
   <h:outputText value="#{msg.type}:"/>
   <h:selectOneMenu value="#{WizardManager.bean.objectType}">
      <f:selectItems value="#{WizardManager.bean.objectTypes}" />
   </h:selectOneMenu>
   
   <h:outputText value=""/>
   <h:outputText value="#{msg.content_type}:"/>
   <h:selectOneMenu id="mime-type" value="#{WizardManager.bean.mimeType}" 
                    valueChangeListener="#{WizardManager.bean.createContentChanged}">
      <f:selectItems value="#{WizardManager.bean.createMimeTypes}" />
   </h:selectOneMenu>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" style="padding-top: 4px;"
             width="100%" rowClasses="wizardSectionHeading, paddingRow"
             rendered="#{WizardManager.bean.otherPropertiesChoiceVisible}">
   <h:outputText value="&nbsp;#{msg.other_properties}" escape="false" />
   <h:outputText value="#{msg.modify_props_help_text}" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" columns="2"
             rendered="#{WizardManager.bean.otherPropertiesChoiceVisible}">
   <h:selectBooleanCheckbox value="#{WizardManager.bean.showOtherProperties}" />
   <h:outputText value="#{msg.modify_props_when_wizard_closes}" />
</h:panelGrid>

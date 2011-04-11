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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

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
      checkButtonState();
   }

   function checkButtonState()
   {
      <%--
      || (document.getElementById("wizard:wizard-body:saveAsTemplate").checked && 
          document.getElementById("wizard:wizard-body:templateName").value.length == 0) )
      --%>
      if (document.getElementById("wizard:wizard-body:name").value.length == 0)
      {
         document.getElementById("wizard:next-button").disabled = true;
         document.getElementById("wizard:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
         document.getElementById("wizard:finish-button").disabled = false;
      }
   }
   
   function toggleTemplateName()
   {
      document.getElementById('wizard:wizard-body:templateName').disabled = 
         !document.getElementById('wizard:wizard-body:saveAsTemplate').checked;
      
      if (document.getElementById('wizard:wizard-body:templateName').disabled == false)
      {
         document.getElementById('wizard:wizard-body:templateName').focus();
      }
      
      checkButtonState();
   }

</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.title_space_details}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="2" cellspacing="2" width="100%">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.name}:"/>
   <h:inputText id="name" value="#{WizardManager.bean.name}" size="35" maxlength="1024" onkeyup="javascript:checkButtonState();" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.title}:" />
   <h:inputText id="title" value="#{WizardManager.bean.title}" size="35" maxlength="1024" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.description}:"/>
   <h:inputText id="description" value="#{WizardManager.bean.description}" size="35" maxlength="1024" />

   <f:verbatim/>
   <h:outputText value="#{msg.icon}:"/>
   <h:panelGrid columns="1" cellpadding="0" cellspacing="0">
      <a:imagePickerRadioPanel id="space-icon" columns="6" spacing="4" value="#{WizardManager.bean.icon}"
                               panelBorder="greyround" panelBgcolor="#F5F5F5">
         <a:listItems value="#{WizardManager.bean.icons}" />
      </a:imagePickerRadioPanel>
   </h:panelGrid>
   <%--
   <tr>
      <td colspan="2">
         <h:selectBooleanCheckbox id="saveAsTemplate" value="#{WizardManager.bean.saveAsTemplate}" 
            onclick="javascript:toggleTemplateName();" />&nbsp;
         <span style="vertical-align:20%"><h:outputText value="#{msg.save_as_template}"/></span>
      </td>
   </tr>
   <tr>
      <td align="middle"><h:outputText value="#{msg.name}"/>:</td>
      <td>
         <h:inputText id="templateName" value="#{WizardManager.bean.templateName}" 
                       size="35" disabled="#{!WizardManager.bean.saveAsTemplate}" 
                       onkeyup="javascript:checkButtonState();" maxlength="1024" />
                       &nbsp;*
      </td>
   </tr>
   --%>
</h:panelGrid>
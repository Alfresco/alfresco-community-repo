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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   var finishButtonPressed = false;
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:name").focus();
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      checkButtonState();
   }

   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:name").value.length == 0 ||
          document.getElementById("wizard:wizard-body:dnsname").value.length < 1 ||
          document.getElementById("wizard:wizard-body:description").value.length > 1000)
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
   
   function validate()
   {
      if (finishButtonPressed)
      {
         finishButtonPressed = false;
         var message = $("wizard:wizard-body:validation_invalid_character").textContent ? $("wizard:wizard-body:validation_invalid_character").textContent : $("wizard:wizard-body:validation_invalid_character").innerText;
         var valid = validateName(document.getElementById("wizard:wizard-body:name"), 
                             message,
                             true);
         if (valid == true)
         {
            message = $("wizard:wizard-body:validation_invalid_dns_name").textContent ? $("wizard:wizard-body:validation_invalid_dns_name").textContent : $("wizard:wizard-body:validation_invalid_dns_name").innerText;
            valid = validateRegex(document.getElementById("wizard:wizard-body:dnsname"),
                  "^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$", true, null,
                  message, true);
         }
         return valid;
      }
      else
      {
         return true;
      }
   }
   
   function toggleDeployToHelp()
   {
      var d = document.getElementById('deploy-to-help');
      d.style.display = d.style.display == 'block' ? 'none' : 'block';
   }
</script>
<h:outputText id="validation_invalid_dns_name" style="display:none" value="#{msg.validation_invalid_dns_name}" />
<table cellpadding="3" cellspacing="2" border="0" width="100%">
   <tr>
      <td colspan="3" class="wizardSectionHeading">
         </f:verbatim>
         <h:outputText value="#{msg.website_details}"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td align="middle">
         </f:verbatim>
         <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.name}:"/>
         <f:verbatim>
      </td>
      <td width="75%">
         </f:verbatim>
         <h:inputText id="name" value="#{WizardManager.bean.name}" size="45" maxlength="1024"
                      onkeyup="javascript:checkButtonState();" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td align="middle">
         </f:verbatim>
         <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.website_dnsname}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="dnsname" value="#{WizardManager.bean.dnsName}" size="45" maxlength="64"
               onkeyup="javascript:checkButtonState();" disabled="#{WizardManager.bean.editMode}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.website_webapp}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:selectOneMenu id="webapp" value="#{WizardManager.bean.webapp}">
            <f:selectItems value="#{WizardManager.bean.webappsList}" />
         </h:selectOneMenu>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.title}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="title" value="#{WizardManager.bean.title}" size="45" maxlength="1024" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td valign="top">
         </f:verbatim>
         <h:outputText value="#{msg.description}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputTextarea id="description" value="#{WizardManager.bean.description}" rows="3" cols="42"
                 onkeyup="javascript:checkButtonState();" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td valign="top">
         </f:verbatim>
         <h:outputText value="#{msg.website_sourcetemplate}"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:selectBooleanCheckbox id="template" value="#{WizardManager.bean.source}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.website_preview_provider}:"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:selectOneMenu id="previewprovider" value="#{WizardManager.bean.previewProvider}">
            <f:selectItems value="#{WizardManager.bean.previewProvidersList}" />
         </h:selectOneMenu>
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>

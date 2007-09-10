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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
   var finishButtonPressed = false;
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:name").focus();
      document.getElementById("wizard").onsubmit = validate;
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      checkButtonState();
   }

   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:name").value.length == 0 ||
          document.getElementById("wizard:wizard-body:dnsname").value.length < 1)
      {
         document.getElementById("wizard:next-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
      }
   }
   
   function validate()
   {
      if (finishButtonPressed)
      {
         finishButtonPressed = false;
         var valid = validateName(document.getElementById("wizard:wizard-body:name"), 
                             '</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" /><f:verbatim>',
                             true);
         if (valid == true)
         {
            valid = validateRegex(document.getElementById("wizard:wizard-body:dnsname"),
                  "^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$", true, null,
                  '</f:verbatim><a:outputText value="#{msg.validation_invalid_dns_name}" /><f:verbatim>', true);
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
      <td width="85%">
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
      <td width="85%">
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
      <td width="85%">
         </f:verbatim>
         <h:inputText id="webapp" value="#{WizardManager.bean.webapp}" size="45" maxlength="256" disabled="true" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td valign="top">
         </f:verbatim>
         <h:outputText value="#{msg.website_deployto}:"/>
         <f:verbatim>
      </td>
      <td width="85%">
         </f:verbatim>
         <h:inputText id="server" value="#{WizardManager.bean.deployTo}" size="45" maxlength="256">
            <a:convertMultiValue />
         </h:inputText>
         <h:graphicImage id="deploy-to-help-img"
		    value="/images/icons/Help_icon.gif" style="cursor:help; padding-left: 4px; vertical-align: -4px;"
		    onclick="javascript:toggleDeployToHelp()" />
         <f:verbatim>
         <div id="deploy-to-help" class="summary infoText statusInfoText" style="display: none; padding: 5px; width: 220px;">
         </f:verbatim>
            <h:outputText id="deploy-top-help-text" value="#{WizardManager.bean.deployToHelp}" escape="false" />
         <f:verbatim>
         </div>
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
         <h:inputTextarea id="description" value="#{WizardManager.bean.description}" rows="3" cols="42" />
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>

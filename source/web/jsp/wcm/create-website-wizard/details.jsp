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
          document.getElementById("wizard:wizard-body:dnsname").value.length < 2)
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
                  "^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]$", true, null,
                  '</f:verbatim><a:outputText value="#{msg.validation_invalid_dns_name}" /><f:verbatim>', true);
         }
         return valid;
      }
      else
      {
         return true;
      }
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
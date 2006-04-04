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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<script language="JavaScript1.2">

   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:name").value.length == 0 )
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }

</script>

<%-- Create Space Dialog Fragment --%>

<a:errors message="#{msg.error_create_space_dialog}" styleClass="errorMessage" />

<f:verbatim>
<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td colspan="2" class="wizardSectionHeading">
         </f:verbatim>
      	<h:outputText value="#{msg.space_props}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.name}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="name" value="#{DialogManager.bean.name}" size="35" maxlength="1024" 
                      onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();"/>
         <f:verbatim>&nbsp;*
      </td>
   </tr>
   <tr>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.description}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="description" value="#{DialogManager.bean.description}" size="35" maxlength="1024" />
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td colspan="2" class="wizardSectionHeading">&nbsp;
         </f:verbatim>
         <h:outputText value="#{msg.other_options}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.choose_space_icon}:" />
         <f:verbatim>
     	</td>
      <td>
         <table border="0" cellpadding="0" cellspacing="0"><tr><td>
         </f:verbatim>
         <a:imagePickerRadioPanel id="space-icon" columns="6" spacing="4" value="#{DialogManager.bean.icon}" 
                                  panelBorder="blue" panelBgcolor="#D3E6FE">
            <a:listItems value="#{DialogManager.bean.icons}" />
         </a:imagePickerRadioPanel>
         <f:verbatim>
         </td></tr></table>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td colspan="2">
         </f:verbatim>
      	<h:outputText value="#{msg.create_space_finish}" />
         <f:verbatim>
      </td>
   </tr>
</table>
<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>

</f:verbatim>
    

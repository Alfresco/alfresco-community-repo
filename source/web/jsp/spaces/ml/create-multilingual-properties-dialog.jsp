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
<%@ taglib uri="http://java.sun.com/jsf/html"      prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core"      prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld"             prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld"                 prefix="r" %>

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
var finishButtonPressed = false;
window.onload = pageLoaded;

function pageLoaded()
{
 document.getElementById("dialog:dialog-body:title").focus();
 document.getElementById("dialog").onsubmit = validate;
 document.getElementById("dialog:finish-button").onclick = function() {finishButtonPressed = true; clear_dialog();}
 checkButtonState();
}

function checkButtonState(){document.getElementById("dialog:finish-button").disabled = false;}

function validate()
{
 if (finishButtonPressed)
 {
  finishButtonPressed = false;
  return validateName(document.getElementById("dialog:dialog-body:title"),
   '</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" /><f:verbatim>', true);
 }
 else
 {
  return true;
 }
}

</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
 <tr>
  <td colspan="3" class="wizardSectionHeading">
   </f:verbatim>
   <h:outputText value="#{msg.properties}" />
   <f:verbatim>
  </td>
 </tr>
 <tr>
  <td >
   </f:verbatim>
   <f:verbatim>
  </td>
  <td>
   </f:verbatim>
   <h:outputText value="#{msg.title}:" />
   <f:verbatim>
  </td>
  <td>
   </f:verbatim>
   <h:inputText id="title" value="#{CreateMultilingualPropertiesBean.title}" size="35" maxlength="1024" onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();" /> 
   <f:verbatim>
  </td>
  <td>
   <f:verbatim>
   <%-- existing properties --%>
   <h:selectOneMenu id="existingpropertiestitle" value="#{CreateMultilingualPropertiesBean.language}" style="width:150px" >
    <f:selectItems value="#{CreateMultilingualPropertiesBean.listAllTitlesProperties}" />
   </h:selectOneMenu>
   </f:verbatim>
  </td>
 </tr>
 
 <tr>
  <td></td>
  <td>
   </f:verbatim>
   <h:outputText value="#{msg.description}:" />
   <f:verbatim>
  </td>
  <td>
   </f:verbatim>
   <h:inputText id="description" value="#{CreateMultilingualPropertiesBean.description}" size="35" maxlength="1024" />
   <f:verbatim>
  </td>
  <td>
   <f:verbatim>
   <%-- existing properties --%>
   <h:selectOneMenu id="existingproperties" value="#{CreateMultilingualPropertiesBean.language}" style="width:150px" >
    <f:selectItems value="#{CreateMultilingualPropertiesBean.listAllDescriptionsProperties}" />
   </h:selectOneMenu>
   </f:verbatim>
  </td>
  
  </br>

 </tr>
</table>
</f:verbatim>  

<table cellpadding="2" cellspacing="2" border="0" width="100%">
 <tr>
  <td colspan="3" style="padding-left:22px">


  <td style="padding-left" >

    <h:selectBooleanCheckbox value="#{CreateMultilingualPropertiesBean.add_new_properties}" id="editnewproperties" />
    
    <span style="vertical-align:20%"></span>
    <h:outputText value=" " />
    
    <h:selectOneMenu id="newlanguage" immediate="true" value="#{CreateMultilingualPropertiesBean.newlanguage}" style="width:80px" >
     <f:selectItems value="#{CreateMultilingualPropertiesBean.contentFilterLanguages}" />
    </h:selectOneMenu>
   
    <h:outputText value=" " />   
    <h:outputText value="#{msg.properties_close}" />
  </td>
</tr>
</table>
  



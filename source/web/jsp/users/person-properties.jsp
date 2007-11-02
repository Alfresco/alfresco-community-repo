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

<f:verbatim>
<script language="JavaScript1.2">
	window.onload = pageLoaded;

	function pageLoaded()
	{
		document.getElementById("wizard:wizard-body:firstName").focus();
		updateButtonState();
	}
	
	function updateButtonState()
	{
		if (document.getElementById("wizard:wizard-body:firstName").value.length == 0 ||
		document.getElementById("wizard:wizard-body:lastName").value.length == 0 ||
		document.getElementById("wizard:wizard-body:email").value.length == 0)
		{
			document.getElementById("wizard:next-button").disabled = true;			
		}
		else
		{
			document.getElementById("wizard:next-button").disabled = false;
		}
	}
</script>
	
<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr>
	<td colspan="2" class="paddingRow"></td></tr>
<tr>
	<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.person_properties}"/><f:verbatim></td>
</tr>
	
<tr>
<td></f:verbatim><h:outputText value="#{msg.first_name}"/><f:verbatim>:</td>
<td></f:verbatim>
<h:inputText id="firstName" value="#{WizardManager.bean.firstName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
</td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.last_name}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputText id="lastName" value="#{WizardManager.bean.lastName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
</td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.email}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputText id="email" value="#{WizardManager.bean.email}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
</td>
</tr>

<tr><td colspan="2" class="paddingRow"></td></tr>
<tr>
<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.other_options}"/><f:verbatim></td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.company_id}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputText value="#{WizardManager.bean.companyId}" size="35" maxlength="1024" /><f:verbatim>
</td>
</tr>

<tr>
	<td></f:verbatim><h:outputText value="Presence Provider"/><f:verbatim>:</td>
	<td>
		</f:verbatim><h:selectOneMenu value="#{WizardManager.bean.presenceProvider}">
			<f:selectItem itemValue="" itemLabel="(None)"/>
			<f:selectItem itemValue="skype" itemLabel="Skype"/>
			<f:selectItem itemValue="yahoo" itemLabel="Yahoo"/>
		</h:selectOneMenu><f:verbatim>
	</td>
</tr>
<tr>
	<td></f:verbatim><h:outputText value="Presence Username"/><f:verbatim>:</td>
	<td>
		</f:verbatim><h:inputText value="#{WizardManager.bean.presenceUsername}" size="35" maxlength="256" /><f:verbatim>
	</td>
</tr>

</table></f:verbatim>
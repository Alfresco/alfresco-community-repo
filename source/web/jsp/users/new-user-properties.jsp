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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<script language="JavaScript1.2">

window.onload = pageLoaded;

function pageLoaded()
{
	if (document.getElementById("wizard:wizard-body:userName") != null &&
		document.getElementById("wizard:wizard-body:userName").disabled == false)
	{
		document.getElementById("wizard:wizard-body:userName").focus();
	}
	else
	{
		document.getElementById("wizard:wizard-body:homeSpaceName").focus();
	}
	updateButtonState();
}

function updateButtonState()
{
	if (document.getElementById("wizard:wizard-body:password") != null &&
		document.getElementById("wizard:wizard-body:password").disabled == false)
	{
		if (document.getElementById("wizard:wizard-body:userName").value.length == 0 ||
			document.getElementById("wizard:wizard-body:password").value.length == 0 ||
			document.getElementById("wizard:wizard-body:confirm").value.length == 0)
		{
			document.getElementById("wizard:finish-button").disabled = true;
			document.getElementById("wizard:next-button").disabled = true;
		}
		else
		{
			document.getElementById("wizard:finish-button").disabled = false;
			document.getElementById("wizard:next-button").disabled = false;
		}
	}
	else
	{
		document.getElementById("wizard:finish-button").disabled = false;
		document.getElementById("wizard:next-button").disabled = false;
	}
}
</script>


<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr><td colspan="2" class="paddingRow"></td></tr>
<tr>
<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.user_properties}"/><f:verbatim></td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.username}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputText id="userName" value="#{WizardManager.bean.userName}" size="35" maxlength="1024" validator="#{LoginBean.validateUsername}" onkeyup="updateButtonState();" onchange="updateButtonState();"/><f:verbatim>&nbsp;*
&nbsp;</f:verbatim><h:message id="errors1" for="userName" style="color:red" /><f:verbatim>
</td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.password}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputSecret id="password" value="#{WizardManager.bean.password}" size="35" maxlength="1024" validator="#{LoginBean.validatePassword}" onkeyup="updateButtonState();" onchange="updateButtonState();" redisplay="true" /><f:verbatim>&nbsp;*
&nbsp;</f:verbatim><h:message id="errors2" for="password" style="color:red" /><f:verbatim>
</td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.confirm}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputSecret id="confirm" value="#{WizardManager.bean.confirm}" size="35" maxlength="1024" validator="#{LoginBean.validatePassword}" onkeyup="updateButtonState();" onchange="updateButtonState();" redisplay="true" /><f:verbatim>&nbsp;*
&nbsp;</f:verbatim><h:message id="errors3" for="confirm" style="color:red" /><f:verbatim>
</td>
</tr>

<tr><td colspan="2" class="paddingRow"></td></tr>
<tr>
<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.homespace}"/><f:verbatim></td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.home_space_location}"/><f:verbatim>:</td>
<td>
</f:verbatim><r:spaceSelector id="space-selector" label="#{msg.select_home_space_prompt}" value="#{WizardManager.bean.homeSpaceLocation}" initialSelection="#{NavigationBean.currentNodeId}" style="border: 1px dashed #cccccc; padding: 2px;"/><f:verbatim>
</td>
</tr>
<tr>
<td></f:verbatim><h:outputText value="#{msg.home_space_name}"/><f:verbatim>:</td>
<td>
</f:verbatim><h:inputText id="homeSpaceName" value="#{WizardManager.bean.homeSpaceName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>
</td>
</tr>
</table></f:verbatim>
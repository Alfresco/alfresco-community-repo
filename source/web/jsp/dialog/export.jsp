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
	document.getElementById("dialog:dialog-body:package-name").focus();
	checkButtonState();
}

function checkButtonState()
{
	if (document.getElementById("dialog:dialog-body:package-name").value.length == 0 ||
		document.getElementById("dialog:dialog-body:destination_selected").value.length == 0)
	{
		document.getElementById("dialog:finish-button").disabled = true;
	}
	else
	{
		document.getElementById("dialog:finish-button").disabled = false;
	}
}
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.package_name}"/><f:verbatim>:</nobr></td>
<td width="90%">
</f:verbatim><h:inputText id="package-name" value="#{DialogManager.bean.packageName}" size="35" maxlength="1024"
onkeyup="javascript:checkButtonState();" /><f:verbatim>
</td>
</tr>
<tr><td class="paddingRow"></td></tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.destination}"/><f:verbatim>:</nobr></td>
<td>
</f:verbatim><r:spaceSelector id="destination" label="#{msg.select_destination_prompt}"
value="#{DialogManager.bean.destination}"
initialSelection="#{NavigationBean.currentNodeId}"
styleClass="selector"/><f:verbatim>
</td>
</tr>
<tr><td class="paddingRow"></td></tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.export_from}:" rendered="#{NavigationBean.currentUser.admin == true}"/><f:verbatim></nobr></td>
<td>
</f:verbatim><h:selectOneRadio value="#{DialogManager.bean.mode}" layout="pageDirection" rendered="#{NavigationBean.currentUser.admin == true}">
<f:selectItem itemValue="all" itemLabel="#{msg.all_spaces_root}" />
<f:selectItem itemValue="current" itemLabel="#{msg.current_space}" />
</h:selectOneRadio><f:verbatim>
</td>
</tr>
<%--
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.encoding}"/><f:verbatim>:</nobr></td>
<td>
</f:verbatim><h:selectOneMenu value="#{DialogManager.bean.encoding}">
<f:selectItems value="#{NewRuleWizard.encodings}" />
</h:selectOneMenu><f:verbatim>
</td>
</tr>
--%>
<tr>
<td>&nbsp;</td>
<td>
</f:verbatim><h:outputText value="<span style='padding-left: 24px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
<h:selectBooleanCheckbox value="#{DialogManager.bean.includeChildren}"/><f:verbatim>&nbsp;
<span style="vertical-align:20%"></f:verbatim><h:outputText value="#{msg.include_children}"/><f:verbatim></span>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td>
</f:verbatim><h:outputText value="<span style='padding-left: 24px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
<h:selectBooleanCheckbox value="#{DialogManager.bean.includeSelf}"/><f:verbatim>&nbsp;
<span style="vertical-align:20%"></f:verbatim><h:outputText value="#{msg.include_self}"/><f:verbatim></span>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td>
</f:verbatim><h:outputText value="<span style='padding-left: 5px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
<h:selectBooleanCheckbox value="#{DialogManager.bean.runInBackground}" /><f:verbatim>&nbsp;
<span style="vertical-align:20%"></f:verbatim><h:outputText value="#{msg.run_export_in_background}"/><f:verbatim></span>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td>
<div id="error-info" style="padding-left: 30px;">
</f:verbatim><h:graphicImage alt="" value="/images/icons/info_icon.gif" style="vertical-align: middle;"/><f:verbatim>&nbsp;&nbsp;</f:verbatim>
<h:outputText value="#{msg.export_error_info}"/><f:verbatim>
</div>
</td>
</tr>
<tr><td class="paddingRow"></td></tr>
</table></f:verbatim>
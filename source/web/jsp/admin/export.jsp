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

<f:verbatim>
<script type="text/javascript">
	var finishButtonPressed = false;
	addEventToElement(window, 'load', pageLoaded, false);
	
	function pageLoaded()
	{
		document.getElementById("dialog:dialog-body:package-name").focus();
		document.getElementById("dialog:finish-button").onclick = function() {finishButtonPressed = true; clear_dialog();}
		checkButtonState();
	}
	
	function checkButtonState()
	{
		if (document.getElementById("dialog:dialog-body:package-name").value.length == 0 ||
			 document.getElementById("destination-value").value.length == 0)
		{
			document.getElementById("dialog:finish-button").disabled = true;
		}
		else
		{
			document.getElementById("dialog:finish-button").disabled = false;
		}
	}
	
	function validate()
	{
	   if (finishButtonPressed)
	   {
	      finishButtonPressed = false;
	      return validateName(document.getElementById("dialog:dialog-body:package-name"), 
	                          unescape('</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" encodeForJavaScript="true" /><f:verbatim>'),
	                          true);
	   }
	   else
	   {
	      return true;
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
</f:verbatim>
<r:ajaxFolderSelector id="destination" label="#{msg.select_destination_prompt}" 
   value="#{DialogManager.bean.destination}" 
   initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
   styleClass="selector" />
<f:verbatim>
</td>
</tr>
<tr><td class="paddingRow"></td></tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.export_from}:" rendered="#{NavigationBean.currentUser.admin == true}"/><f:verbatim></nobr></td>
<td>
</f:verbatim><h:selectOneRadio value="#{DialogManager.bean.mode}" layout="pageDirection" rendered="#{NavigationBean.currentUser.admin == true}">
<%--<f:selectItem itemValue="all" itemLabel="#{msg.all_spaces_root}" itemDisabled="true" />--%>
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
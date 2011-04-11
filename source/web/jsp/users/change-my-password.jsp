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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<f:verbatim>
	<script type="text/javascript">

   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:old-password").focus();
      updateButtonState();
   }
   
   function updateButtonState()
   {
      if (document.getElementById("dialog:dialog-body:password").value.length == 0 ||
          document.getElementById("dialog:dialog-body:old-password").value.length == 0 ||
          document.getElementById("dialog:dialog-body:confirm").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>

	<table cellspacing="0" cellpadding="3" border="0" width="100%">
		<tr>
			

<td width="100%" valign="top">
	<table cellpadding="2" cellspacing="2" border="0" width="100%">
		<tr>
			<td colspan="2"></f:verbatim><h:outputText
				value="#{msg.change_my_password_instructions}" /><f:verbatim></td>
		</tr>
		<tr>
			<td colspan="2" class="paddingRow"></td>
		</tr>
		<tr>
			<td></f:verbatim><h:outputText value="#{msg.username}" /><f:verbatim>:</td>
			<td></f:verbatim><h:outputText
				value="#{UsersBeanProperties.person.properties.userName}" /><f:verbatim></td>
		</tr>
		<tr>
			<td></f:verbatim><h:outputText value="#{msg.old_password}" /><f:verbatim>:</td>
			<td></f:verbatim><h:inputSecret id="old-password"
				value="#{UsersBeanProperties.oldPassword}" size="35" maxlength="255"
				validator="#{LoginBean.validatePassword}"
				onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
				&nbsp;</f:verbatim><h:message id="errors0" for="old-password" style="color:red" /><f:verbatim></td>
		</tr>
		<tr>
			<td></f:verbatim><h:outputText value="#{msg.new_password}" /><f:verbatim>:</td>
			<td></f:verbatim><h:inputSecret id="password" value="#{UsersBeanProperties.password}"
				size="35" maxlength="255" validator="#{LoginBean.validatePassword}"
				onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
				&nbsp;</f:verbatim><h:message id="errors1" for="password" style="color:red" /><f:verbatim></td>
		</tr>
		<tr>
			<td></f:verbatim><h:outputText value="#{msg.confirm}" /><f:verbatim>:</td>
			<td></f:verbatim><h:inputSecret id="confirm" value="#{UsersBeanProperties.confirm}"
				size="35" maxlength="255" validator="#{LoginBean.validatePassword}"
				onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
				&nbsp;</f:verbatim><h:message id="errors2" for="confirm" style="color:red" /><f:verbatim></td>
		</tr>
	</table>
	</td>
	</tr>
	</table>
</f:verbatim>
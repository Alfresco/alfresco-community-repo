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
	document.getElementById("dialog:dialog-body:first-name").focus();
	updateButtonState();
}

function updateButtonState()
{
	if (document.getElementById("dialog:dialog-body:first-name").value.length == 0 ||
		document.getElementById("dialog:dialog-body:last-name").value.length == 0 ||
		document.getElementById("dialog:dialog-body:email").value.length == 0)
	{
		document.getElementById("dialog:finish-button").disabled = true;
	}
	else
	{
		document.getElementById("dialog:finish-button").disabled = false;
	}
}
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.person_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="2" cellspacing="2" width="100%">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.first_name}:"/>
   <h:inputText id="first-name" value="#{DialogManager.bean.firstName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.last_name}:"/>
   <h:inputText id="last-name" value="#{DialogManager.bean.lastName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.email}"/>
   <h:inputText id="email" value="#{DialogManager.bean.email}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
</h:panelGrid>

<f:verbatim>
<script>
   updateButtonState();
</script>
</f:verbatim>
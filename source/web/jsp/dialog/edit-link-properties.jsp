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
<script type="text/javascript">
function checkButtonState()
{
if (document.getElementById("dialog:dialog-body:file-name").value.length == 0)
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
<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.link_properties}" /><f:verbatim></td>
</tr>
<tr><td colspan="2" class="paddingRow"></td></tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.file_name}" /><f:verbatim>:</nobr></td>
<td width="90%">
</f:verbatim><h:inputText id="file-name" value="#{DialogManager.bean.properties.name}" size="35" maxlength="1024"
onkeyup="javascript:checkButtonState();" /><f:verbatim>&nbsp;*
</td>
</tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.title}" /><f:verbatim>:</nobr></td>
<td>
</f:verbatim><h:inputText id="title" value="#{DialogManager.bean.properties.title}" size="35" maxlength="1024" /><f:verbatim>
</td>
</tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.description}" /><f:verbatim>:</nobr></td>
<td>
</f:verbatim><h:inputText value="#{DialogManager.bean.properties.description}" size="35" maxlength="1024" /><f:verbatim>
</td>
</tr>
<tr>
<td><nobr></f:verbatim><h:outputText value="#{msg.link_destination}" /><f:verbatim>:</nobr></td>
<td>
</f:verbatim><h:outputText value="#{DialogManager.bean.destinationPath}" /><f:verbatim>
</td>
</tr>
</table></f:verbatim>
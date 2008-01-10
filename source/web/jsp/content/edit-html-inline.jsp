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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">

<%-- Init the Tiny MCE in-line HTML editor --%>
tinyMCE.init({
theme : "advanced",
mode : "exact",
relative_urls: false,
elements : "editor",
save_callback : "saveContent",
plugins : "table",
theme_advanced_toolbar_location : "top",
theme_advanced_toolbar_align : "left",
theme_advanced_buttons1_add : "fontselect,fontsizeselect",
theme_advanced_buttons2_add : "separator,forecolor,backcolor",
theme_advanced_buttons3_add_before : "tablecontrols,separator",
theme_advanced_disable: "styleselect",
extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
});

function saveContent(id, content)
{
 //document.forms['edit-file']['edit-file:editorOutput'].value = content;
 document.getElementById("dialog:dialog-body:editorOutput").value=content;
}

</script>

<table cellspacing="0" cellpadding="3" border="0" width="100%">
	<tr>
		<td width="100%" valign="top">
			<%-- Hide the checkout info if this document is already checked out --%>
			</f:verbatim><a:panel id="checkout-panel" rendered="#{CCProperties.document.properties.workingCopy == false}"><f:verbatim>
			<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td valign=top style="padding-top:2px" width=20></f:verbatim>
						<h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/><f:verbatim></td>
						<td><td class="mainSubText">
						</f:verbatim><h:outputText value="#{msg.you_may_want}" />
						<h:outputText value=" "/>
						<a:actionLink value="#{msg.checkout_document}" actionListener="#{CCEditHtmlInlineDialog.setupContentAction}" action="dialog:checkoutFile">
						<f:param name="id" value="#{CCProperties.document.id}" />
						</a:actionLink>
						<h:outputText value=" "/>
						<h:outputText value="#{msg.checkout_want_to}" /><f:verbatim>
						<br></f:verbatim>
						<h:outputText value="#{msg.checkout_warn}" /><f:verbatim>
						</td>
						</td>
					</tr>
				</table>
			<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
			</f:verbatim></a:panel><f:verbatim>
		</td>
	</tr>

	<%-- Inline editor --%>
	<tr>
		<td width="100%" valign="top" height="100%">
			<div id='editor' style='width:100%; height:360px'>
			</f:verbatim><h:outputText value="#{CCProperties.documentContent}" escape="false" /><f:verbatim>
			</div>
			</f:verbatim><h:inputHidden id="editorOutput" value="#{CCProperties.editorOutput}" /><f:verbatim>
		</td>
	</tr>
</table>
</f:verbatim>
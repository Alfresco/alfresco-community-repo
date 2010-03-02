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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
<table cellspacing="0" cellpadding="3" border="0" width="100%">
	<tr>
		<td width="100%" valign="top">
		<%-- Hide the checkout info if this document is already checked out --%>
			</f:verbatim><a:panel id="checkout-panel" rendered="#{CCProperties.document.properties.workingCopy == false}"><f:verbatim>
				<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
					<td valign=top style="padding-top:2px" width=20></f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/><f:verbatim></td>
					<td><td class="mainSubText">
					</f:verbatim><h:outputText value="#{msg.you_may_want}" />
					<h:outputText value=" "/>
					<a:actionLink value="#{msg.checkout_document}" actionListener="#{CCEditTextInlineDialog.setupContentAction}" action="dialog:checkoutFile">
					<f:param name="id" value="#{CCProperties.document.id}" />
					</a:actionLink>
					<h:outputText value=" "/>
					<h:outputText value="#{msg.checkout_want_to}" /><f:verbatim>
					<br>
					</f:verbatim><h:outputText value="#{msg.checkout_warn}" /><f:verbatim>
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
			<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
			</f:verbatim><h:inputTextarea id="textArea" rows="24" cols="120" value="#{CCProperties.editorOutput}" /><f:verbatim>
			<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
		</td>
	</tr>
</table>
</f:verbatim>

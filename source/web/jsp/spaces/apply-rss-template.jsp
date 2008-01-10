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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

<f:verbatim>
	<table cellspacing="0" cellpadding="0" border="0" width="100%">
		<tr>
			<%-- TODO: check for Guest user access and hide panel? --%>
			<td width="100%" valign="top">
			<%
						PanelGenerator.generatePanelStart(out, request.getContextPath(),
						"yellowInner", "#ffffcc");
			%>
			<table cellpadding="0"  cellspacing="0" border="0" width="100%">
				<tr>
					<td valign=top style="padding-top:2px" width=20></f:verbatim><h:graphicImage
						url="/images/icons/info_icon.gif" width="16" height="16" /><f:verbatim></td>
					<td class="mainSubText"></f:verbatim><h:outputText
						value="#{msg.apply_rss_feed_warning1} " /> <a:actionLink
						value="#{msg.manage_invited_users}"
						actionListener="#{BrowseBean.setupSpaceAction}"
						action="dialog:manageInvitedUsers">
						<f:param name="id" value="#{SpaceDetailsDialog.id}" />
					</a:actionLink> <h:outputText value=" #{msg.apply_rss_feed_warning2}" /><f:verbatim></td>
				</tr>
			</table>
			<%
						PanelGenerator.generatePanelEnd(out, request.getContextPath(),
						"yellowInner");
			%>

			<div style="padding:4px"></div>
			<%
						PanelGenerator.generatePanelStart(out, request.getContextPath(),
						"white", "white");
			%>
			<table cellpadding="2" cellspacing="2" border="0" width="100%">
				<tr>
					<td><nobr></f:verbatim><h:outputText value="#{msg.rss_template}" /><f:verbatim>:</nobr></td>
					<td width=100%><%-- Templates drop-down selector --%> </f:verbatim><h:selectOneMenu
						value="#{ApplyRssTemplateDialog.RSSTemplate}">
						<f:selectItems value="#{TemplateSupportBean.RSSTemplates}" />
					</h:selectOneMenu><f:verbatim></td>
				</tr>
			</table>
			<%
						PanelGenerator.generatePanelEnd(out, request.getContextPath(),
						"white");
			%>
			</td>
		</tr>
	</table>
</f:verbatim>

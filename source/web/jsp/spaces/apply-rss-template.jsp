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

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<r:loadBundle var="msg"/>

<f:verbatim>
<script type="text/javascript">
	var finishButtonPressed = false;
	addEventToElement(window, 'load', pageLoaded, false);
	
	function pageLoaded()
	{
		checkButtonState();
	}
	
	function checkButtonState()
	{
		if (document.getElementById("dialog:dialog-body:template").value == "none")
		{
			document.getElementById("dialog:finish-button").disabled = true;
		}
		else
		{
			document.getElementById("dialog:finish-button").disabled = false;
		}
	}
</script>

   <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
   	<table cellpadding="0"  cellspacing="0" border="0" width="100%">
   		<tr>
   			<td valign="top" style="padding-top:2px" width="20"></f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16" /><f:verbatim></td>
   			<td class="mainSubText"></f:verbatim>
   			   <h:outputText value="#{msg.apply_rss_feed_warning1}" />
   			   <a:actionLink value=" #{msg.manage_invited_users} " actionListener="#{BrowseBean.setupSpaceAction}" action="dialog:manageInvitedUsers">
   				   <f:param name="id" value="#{SpaceDetailsDialog.id}" />
   			   </a:actionLink>
   			   <h:outputText value="#{msg.apply_rss_feed_warning2}" /><f:verbatim></td>
   		</tr>
   	</table>
	<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
	<div style="padding:4px"></div>
	<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %></f:verbatim>
      <h:panelGrid id="template-panel" columns="2" cellpadding="2" cellspacing="2" border="0" width="100%" columnClasses="panelGridLabelColumnAuto,panelGridValueColumn">
         <h:outputText value="#{msg.rss_template}:" />
      	<h:selectOneMenu id="template" value="#{ApplyRssTemplateDialog.RSSTemplate}" onchange="checkButtonState();">
      		<f:selectItems value="#{TemplateSupportBean.RSSTemplates}" />
      	</h:selectOneMenu>
      </h:panelGrid><f:verbatim>
	<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
</f:verbatim>
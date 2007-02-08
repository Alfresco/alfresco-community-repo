<%--
  Copyright (C) 2005 Alfresco, Inc.
 
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="8kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGroup rendered="#{BrowseBean.deleteMessage != null}">
   <f:verbatim>
   <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
   <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
         <td valign=top style="padding-top:2px" width=20>
            </f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/><f:verbatim>
         </td>
         <td class="mainSubText">
            </f:verbatim><h:outputText value="#{BrowseBean.deleteMessage}" /><f:verbatim>
         </td>
      </tr>
   </table>
   <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
   <br/>
   </f:verbatim>
</h:panelGroup>

<h:outputText value="#{msg.select_delete_operation}" styleClass="mainSubTitle" />
<h:panelGrid columns="1" cellpadding="2" cellspacing="2" border="0">
   <h:selectOneRadio id="delete-operation" layout="pageDirection" value="#{DialogManager.bean.deleteMode}">
      <f:selectItem itemValue="all" itemLabel="#{msg.delete_op_all}" />
      <f:selectItem itemValue="files" itemLabel="#{msg.delete_op_files}" />
      <f:selectItem itemValue="folders" itemLabel="#{msg.delete_op_folders}" />
      <f:selectItem itemValue="contents" itemLabel="#{msg.delete_op_contents}" />
   </h:selectOneRadio>
</h:panelGrid>

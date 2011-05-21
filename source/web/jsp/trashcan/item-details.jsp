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

<f:verbatim><div style="padding:4px"></f:verbatim>
<h:outputText value="#{msg.original_location}: " styleClass="mainSubTitle" />
<r:nodePath value="#{TrashcanDialogProperty.item.properties.locationPath}" breadcrumb="true" actionListener="#{BrowseBean.clickSpacePath}" showLeaf="true" />
<f:verbatim></div></f:verbatim>

<f:verbatim>
   <table cellspacing="0" cellpadding="3" border="0" width="100%">
      <tr>
         <td width="100%" valign="top">
         </f:verbatim>
<a:panel label="#{msg.view_links}" id="link-panel" progressive="true"
border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
            <f:verbatim>
            <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
<tr>
<td>
            </f:verbatim>
                     <a:actionLink value="#{msg.view_in_browser}" href="#{TrashcanItemDetailsDialog.itemBrowserUrl}" target="new" id="link1" rendered="#{TrashcanDialogProperty.item.properties.isFolder == false}" />
            <f:verbatim>
                  </td>
<td>
            </f:verbatim>
                     <a:actionLink value="#{msg.download_content}" href="#{TrashcanItemDetailsDialog.itemDownloadUrl}" target="new" id="link2" rendered="#{TrashcanDialogProperty.item.properties.isFolder == false}" />
            <f:verbatim>
                  </td>
<td>
<a href='</f:verbatim><a:outputText value="#{TrashcanItemDetailsDialog.itemNodeRefUrl}" id="out1" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.noderef_link}" id="out4" /><f:verbatim></a>
</td>
</tr>
</table>
</f:verbatim>
</a:panel>

<f:verbatim><div style="padding:4px"></div></f:verbatim>
            
<a:panel label="#{msg.properties}" id="properties-panel" progressive="true"
border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
<f:verbatim>
<table cellspacing="0" cellpadding="0" border="0" width="100%">
<tr>
                  <td width="80" align="center">
<%-- icon image for the object --%>
                     <table cellspacing="0" cellpadding="0" border="0">
<tr>
<td>
<div style="border: thin solid #CCCCCC; padding:4px">
            </f:verbatim>
                                 <a:actionLink id="logo-content" value="#{TrashcanDialogProperty.item.name}" href="#{TrashcanItemDetailsDialog.itemDownloadUrl}" target="new" rendered="#{TrashcanDialogProperty.item.properties.isFolder == false}"
image="#{TrashcanDialogProperty.item.properties.icon}" showLink="false" />
<a:actionLink id="logo-folder" value="#{TrashcanDialogProperty.item.name}" rendered="#{TrashcanDialogProperty.item.properties.isFolder == true}"
image="#{TrashcanDialogProperty.item.properties.icon}" showLink="false" />
<f:verbatim>
</div>
</td>
                           <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width="6" height="42"></td>
</tr>
<tr>
                           <td colspan="2"><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width="48" height="5"></td>
</tr>
</table>
</td>
<td>
<%-- properties for the item --%>
            </f:verbatim>
                  <r:propertySheetGrid id="item-props" value="#{TrashcanDialogProperty.item}" var="itemProps"
columns="1" mode="view" labelStyleClass="propertiesLabel" externalConfig="true" />
<h:messages globalOnly="true" id="props-msgs" styleClass="errorMessage" layout="table" />
<h:message for="item-props" styleClass="statusMessage" />
<f:verbatim>
</td>
</tr>
</table>
</f:verbatim>
</a:panel>
         <f:verbatim>
         </td>
         <td valign="top">
         </f:verbatim>
         <%-- Document Actions --%>
         <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true" expanded="true">
            <r:actions id="actions_doc" value="deleteditem_actions" context="#{DialogManager.bean.item}" verticalSpacing="3" style="white-space:nowrap" />
         </a:panel>
         <f:verbatim>
         </td>
      </tr>
   </table>
</f:verbatim>


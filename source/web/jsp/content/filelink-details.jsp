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


<f:verbatim>
<table cellspacing="0" cellpadding="3" border="0" width="100%">
   <tr>
      <td width="100%" valign="top"></f:verbatim>
      <a:panel label="#{msg.view_links}" id="preview-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
         expanded='#{DialogManager.bean.panels["preview-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <f:verbatim>
         <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
            <tr>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_browser}" href="#{DialogManager.bean.browserUrl}" target="new" id="link1" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_webdav}" href="#{DialogManager.bean.webdavUrl}" target="new" id="link2" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_cifs}" href="#{DialogManager.bean.cifsPath}" target="new" id="link3" /><f:verbatim></td>
            </tr>
            <tr>
               <td></f:verbatim><a:actionLink value="#{msg.download_content}" href="#{DialogManager.bean.downloadUrl}" target="new" id="link4" /><f:verbatim></td>
               <td><a href='<%=request.getContextPath()%></f:verbatim><a:outputText value="#{DialogManager.bean.bookmarkUrl}" id="out1" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.details_page_bookmark}" id="out2" /><f:verbatim></a></td>
               <td><a href='</f:verbatim><a:outputText value="#{DialogManager.bean.nodeRefUrl}" id="out3" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.noderef_link}" id="out4" /><f:verbatim></a></td>
            </tr></f:verbatim>
            <a:panel id="link-panel" rendered="#{NavigationBean.inPortalServer == false}"><f:verbatim>
               <tr>
                  <td colspan=3><a href='<%=request.getContextPath()%></f:verbatim><a:outputText value="#{LinkPropertiesDialog.fileLinkBookmarkUrl}" id="out5" /><f:verbatim>'></f:verbatim><a:outputText value="#{msg.link_destination_details}" id="out6" /><f:verbatim></a></td>
               </tr>
               </f:verbatim>
            </a:panel>
            <f:verbatim>
         </table>
         </f:verbatim>
      </a:panel>

      <f:verbatim><div style="padding: 4px"></div></f:verbatim>

      <h:panelGroup id="props-panel-facets">
         <f:facet name="title">
            <r:permissionEvaluator value="#{DialogManager.bean.document}" allow="Write" id="evalChange">
               <a:actionLink id="titleLink10" value="#{msg.modify}" showLink="false" image="/images/icons/Change_details.gif" action="dialog:editLinkProperties" actionListener="#{DialogManager.setupParameters}" >
                   <f:param name="nodeRef" value="#{DialogManager.bean.document.nodeRefAsString}" id="param1" />
               </a:actionLink>
            </r:permissionEvaluator>
         </f:facet>
      </h:panelGroup> 
      <a:panel label="#{msg.properties}" id="properties-panel" facetsId="dialog:dialog-body:props-panel-facets" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
         rendered="#{DialogManager.bean.locked == false}" expanded='#{DialogManager.bean.panels["properties-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <f:verbatim>
         <table cellspacing="0" cellpadding="0" border="0" width="100%">
            <tr>
               <td width=80 align=center><%-- icon image for the doc --%>
               <table cellspacing=0 cellpadding=0 border=0>
                  <tr>
                     <td>
                     <div style="border: thin solid #CCCCCC; padding: 4px"></f:verbatim><a:actionLink id="doc-logo1" value="#{DialogManager.bean.name}" href="#{DialogManager.bean.url}" target="new"
                        image="#{DialogManager.bean.document.properties.fileType32}" showLink="false" /><f:verbatim></div>
                     </td>
                     <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width=6 height=42></td>
                  </tr>
                  <tr>
                     <td colspan=2><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width=48 height=5></td>
                  </tr>
               </table>
               </td>
               <td><%-- properties for the doc link --%> 
               </f:verbatim><r:propertySheetGrid id="document-props" value="#{DialogManager.bean.document}" var="documentProps" columns="1" mode="view" labelStyleClass="propertiesLabel" externalConfig="true" /> <h:messages
                  globalOnly="true" id="props-msgs" styleClass="errorMessage" layout="table" /> <h:message for="document-props" styleClass="statusMessage" /><f:verbatim></td>
            </tr>
         </table></f:verbatim>
      </a:panel><f:verbatim></td>
     
      <td valign="top"></f:verbatim>

      <%-- Document Actions --%> 
      <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true"
         expanded='#{DialogManager.bean.panels["actions-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <r:actions id="actions_link_doc" value="filelink_details_actions" context="#{DialogManager.bean.document}" verticalSpacing="3" style="white-space:nowrap" />
      </a:panel><f:verbatim></td>
   </tr>
</table>
</f:verbatim>

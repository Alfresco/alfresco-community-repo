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
      <a:panel label="#{msg.links}" id="preview-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
         expanded='#{DialogManager.bean.panels["preview-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <f:verbatim>
         <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
            <tr>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_webdav}" href="#{DialogManager.bean.webdavUrl}" target="new" id="link1" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_cifs}" href="#{DialogManager.bean.cifsPath}" target="new" id="link2" /><f:verbatim></td>
               <td><a href='<%=request.getContextPath()%></f:verbatim><a:outputText value="#{DialogManager.bean.bookmarkUrl}" id="out1" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.details_page_bookmark}" id="out2" /><f:verbatim></a></td>
               <td><a href='</f:verbatim><a:outputText value="#{DialogManager.bean.nodeRefUrl}" id="out3" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.noderef_link}" id="out4" /><f:verbatim></a></td>
            </tr></f:verbatim>
            <a:panel id="link-panel" rendered="#{NavigationBean.inPortalServer == false}"><f:verbatim>
               <tr>
                  <td colspan=4><a href='<%=request.getContextPath()%></f:verbatim><a:outputText value="#{LinkPropertiesDialog.spaceLinkDestinationUrl}" id="out5" /><f:verbatim>'></f:verbatim><a:outputText value="#{msg.link_destination_details}" id="out6" /><f:verbatim></a></td>
               </tr></f:verbatim>
            </a:panel>
            <f:verbatim>
         </table>
         </f:verbatim>
      </a:panel>

      <f:verbatim><div style="padding: 4px"></div></f:verbatim>

      <%-- wrapper comment used by the panel to add additional component facets --%> 
      <h:panelGroup id="props-panel-facets">
         <f:facet name="title">
            <r:permissionEvaluator value="#{DialogManager.bean.space}" allow="Write" id="evalProps">
               <a:actionLink id="titleLink10" value="#{msg.modify}" showLink="false" image="/images/icons/Change_details.gif" action="dialog:editLinkProperties" actionListener="#{DialogManager.setupParameters}" >
                <f:param name="nodeRef" value="#{DialogManager.bean.space.nodeRefAsString}" id="param0" />
               </a:actionLink>
            </r:permissionEvaluator>
         </f:facet>
      </h:panelGroup> 
      <a:panel label="#{msg.properties}" id="properties-panel" facetsId="dialog:dialog-body:props-panel-facets" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true"
         expanded='#{DialogManager.bean.panels["properties-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <f:verbatim>
         <table cellspacing="0" cellpadding="0" border="0" width="100%">
            <tr>
               <td width=80 align=center><%-- icon image for the space --%>
               <table cellspacing=0 cellpadding=0 border=0>
                  <tr>
                     <td>
                     <div style="border: thin solid #CCCCCC; padding: 4px"></f:verbatim><h:graphicImage id="space-logo" url="/images/icons/#{DialogManager.bean.space.properties.icon}.gif" width="32" height="32" /><f:verbatim></div>
                     </td>
                     <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width=6 height=42></td>
                  </tr>
                  <tr>
                     <td colspan=2><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width=48 height=5></td>
                  </tr>
               </table>
               </td>
               <td>
               <%-- properties for the space link --%>
                </f:verbatim>
                <r:propertySheetGrid id="space-props" value="#{DialogManager.bean.space}" var="spaceProps" columns="1" mode="view" labelStyleClass="propertiesLabel" externalConfig="true" /> 
                <f:verbatim></td>
                  
            </tr>
         </table>
         </f:verbatim>
      </a:panel><f:verbatim></td>

      <td valign="top">

</f:verbatim>
      <%-- Space Actions --%> 
      <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true"
         expanded='#{DialogManager.bean.panels["actions-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">
         <r:actions id="actions_link_space" value="spacelink_details_actions" context="#{DialogManager.bean.space}" verticalSpacing="3" style="white-space:nowrap" />
      </a:panel><f:verbatim></td>
   </tr>
</table>
</f:verbatim>

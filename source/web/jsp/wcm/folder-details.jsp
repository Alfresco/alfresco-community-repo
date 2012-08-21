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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<f:verbatim>
<table cellspacing="0" cellpadding="3" border="0" width="100%">
   <tr>
      <td width="100%" valign="top"></f:verbatim>
      <a:panel label="#{msg.view_links}" id="links-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{FolderDetailsBean.panels["links-panel"]}'
         expandedActionListener="#{FolderDetailsBean.expandPanel}"><f:verbatim>
         <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
            <tr>
               <td></f:verbatim><a:actionLink value="#{msg.folder_preview}" href="#{FolderDetailsBean.previewUrl}" target="new" id="link3" /><f:verbatim></td>
               <td><a href='</f:verbatim><a:outputText value="#{FolderDetailsBean.nodeRefUrl}" id="out3" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.noderef_link}" id="out4" /><f:verbatim></a></td>
            </tr>
         </table></f:verbatim>
      </a:panel>

      <f:verbatim><div style="padding: 4px"></div></f:verbatim>

      <h:panelGroup id="props-panel-facets">
         <f:facet name="title">
            <r:permissionEvaluator id="eval1" value="#{FolderDetailsBean.folder}" allow="Write">
               <a:actionLink id="titleLink1" value="#{msg.modify}" showLink="false" image="/images/icons/Change_details.gif" action="dialog:editAvmFolderProperties" />
            </r:permissionEvaluator>
         </f:facet>
      </h:panelGroup> 
      
      <a:panel label="#{msg.properties}" id="properties-panel" facetsId="dialog:dialog-body:props-panel-facets" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{FolderDetailsBean.panels["properties-panel"]}'
         expandedActionListener="#{FolderDetailsBean.expandPanel}"><f:verbatim>
         <table cellspacing="0" cellpadding="0" border="0" width="100%">
            <tr>
               <td width=80 align=center>
               <%-- icon image for the folder --%>
               <table cellspacing=0 cellpadding=0 border=0>
                  <tr>
                     <td>
                     <div style="border: thin solid #CCCCCC; padding: 4px"></f:verbatim>
                        <h:graphicImage id="space-logo" url="/images/icons/space-icon-default.gif" width="32" height="32" /><f:verbatim>
                     </div>
                     </td>
                     <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width=6 height=42></td>
                  </tr>
                  <tr>
                     <td colspan=2><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width=48 height=5></td>
                  </tr>
               </table>
               </td>
               <td></f:verbatim>
               <%-- properties for the folder --%> 
                  <r:propertySheetGrid id="folder-props" value="#{FolderDetailsBean.folder}" var="folderProps" columns="1" mode="view" labelStyleClass="propertiesLabel" externalConfig="true" /><f:verbatim>
               </td>
            </tr>
         </table></f:verbatim>
      </a:panel><f:verbatim>
      </td>

      <td valign="top"></f:verbatim>

      <%-- Document Actions --%> 
      <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true" expanded='#{FolderDetailsBean.panels["actions-panel"]}'
         expandedActionListener="#{FolderDetailsBean.expandPanel}">
         <r:actions id="actions_doc" value="avm_folder_details" context="#{FolderDetailsBean.avmNode}" verticalSpacing="3" style="white-space:nowrap" />
      </a:panel><f:verbatim>
      </td>
   </tr>
</table></f:verbatim>
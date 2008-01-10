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

<f:verbatim>
   <table cellspacing="0" cellpadding="3" border="0" width="100%">
      <tr>
         <td width="100%" valign="top"><%-- wrapper comment used by the panel to add additional component facets --%></f:verbatim> <h:panelGroup id="dashboard-panel-facets">
            <f:facet name="title">
               <r:permissionEvaluator value="#{DialogManager.bean.document}" allow="Write" id="evalChange">
                  <a:actionLink id="actModify" value="#{msg.modify}" action="dialog:applyTemplate" showLink="false" image="/images/icons/preview.gif" style="padding-right:8px" />
                  <a:actionLink id="actRemove" value="#{msg.remove}" actionListener="#{DialogManager.bean.removeTemplate}" showLink="false" image="/images/icons/delete.gif" />
               </r:permissionEvaluator>
            </f:facet>
         </h:panelGroup> 
         <%--
         Multlingual details
         --%> 
         <%-- properties for Ml container --%> 
         <h:panelGroup id="ml-props-panel-facets">
            <f:facet name="title">
               <r:permissionEvaluator value="#{DialogManager.bean.document}" allow="Write" id="evalModify">
                  <a:actionLink id="titleLinkMl" value="#{msg.modify}" showLink="false" image="/images/icons/edit_properties.gif" action="dialog:editMlContainer" />
               </r:permissionEvaluator>
            </f:facet>
         </h:panelGroup> 
         <a:panel label="#{msg.properties}" facetsId="dialog:dialog-body:ml-props-panel-facets" id="ml-properties-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
            expanded='#{DialogManager.bean.panels["ml-properties-panel"]}' expandedActionListener="#{DialogManager.bean.expandPanel}">


            <h:outputText styleClass="nodeWorkflowInfoTitle" />
            <r:propertySheetGrid id="ml-container-props-sheet" value="#{DialogManager.bean.document}" var="mlContainerProps" columns="1" labelStyleClass="propertiesLabel" externalConfig="true" cellpadding="2" cellspacing="2" mode="view" />
         </a:panel> 
         <f:verbatim>
            <div style="padding: 4px"></div>
         </f:verbatim> 
         <%-- list of translations --%> 
         <a:panel label="#{msg.translations}" id="ml-translation-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{DialogManager.bean.panels["ml-translation-panel"]}'
            expandedActionListener="#{DialogManager.bean.expandPanel}">

            <a:richList id="TranslationList" viewMode="details" value="#{DialogManager.bean.translations}" var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="Name"
               initialSortDescending="false">

               <%-- Icon details view mode --%>
               <a:column id="col20" primary="true" width="20" style="padding:2px;">
                  <f:facet name="header">
                     <h:outputText id="outT" value="" />
                  </f:facet>
                  <h:graphicImage url="/images/filetypes/_default.gif" />
               </a:column>

               <%-- Name Columns --%>
               <a:column id="col21" width="300" style="text-align:left">
                  <f:facet name="header">
                     <a:sortLink id="sl1" label="#{msg.name}" value="Name" mode="case-insensitive" styleClass="header" />
                  </f:facet>
                  <a:actionLink id="view-name" value="#{r.name}" href="#{r.url}" target="new" />
               </a:column>

               <%-- Language columns --%>
               <a:column id="col22" width="50" style="text-align:left">
                  <f:facet name="header">
                     <a:sortLink id="sl2" label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header" />
                  </f:facet>
                  <h:outputText id="view-language" value="#{r.language}" />
               </a:column>

               <%-- view actions --%>
               <a:column id="col25" style="text-align: left">
                  <f:facet name="header">
                     <h:outputText id="outT2" value="#{msg.actions}" />
                  </f:facet>
                  <a:actionLink id="view-link" value="#{msg.view}" href="#{r.url}" target="new" />
                  <%--
                                                Start the new edition wizard from this translation
                                          --%>
                  <h:outputText id="space" value=" " />
                  <a:actionLink id="new-edition-from" value="#{msg.new_edition}" action="wizard:newEditionFrom" actionListener="#{NewEditionWizard.skipFirstStep}" rendered="#{r.notEmpty && r.userHasRight}">
                     <f:param id="par1" name="lang" value="#{r.language}" />
                  </a:actionLink>
               </a:column>

               <a:dataPager id="pager" styleClass="pager" />
            </a:richList>
         </a:panel> 
         <f:verbatim>
            <div style="padding: 4px"></div>
         </f:verbatim> 
         <%--
         Editions details
         --%> 
         <a:panel label="#{msg.editions}" id="ml-editions-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{DialogManager.bean.panels["ml-editions-panel"]}'
            expandedActionListener="#{DialogManager.bean.expandPanel}">

            <a:richList id="EditionTitle" viewMode="details" value="#{DialogManager.bean.emptyListAndInitEditions}" var="ed" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" initialSortDescending="true">

               <%-- Icon details view mode --%>
               <a:column id="col30" primary="true" width="20" style="padding:2px;">
                  <f:facet name="header">
                     <h:outputText id="outT3" value="" />
                  </f:facet>
               </a:column>

               <%-- edition label --%>
               <a:column id="col31" width="100" style="text-align:left">
                  <f:facet name="header">
                     <h:outputText id="outT4" value="#{msg.edition}" styleClass="header" />
                  </f:facet>
               </a:column>

               <%-- edition notes --%>
               <a:column id="col32" width="200" style="text-align:left">
                  <f:facet name="header">
                     <h:outputText id="outT5" value="#{msg.notes}" styleClass="header" />
                  </f:facet>
               </a:column>

               <%-- edition author --%>
               <a:column id="col33" style="text-align:left">
                  <f:facet name="header">
                     <h:outputText id="outT6" value="#{msg.author}" styleClass="header" />
                  </f:facet>
               </a:column>

               <%-- edition date --%>
               <a:column id="col34" style="text-align:left; white-space:nowrap">
                  <f:facet name="header">
                     <h:outputText id="outT7" value="#{msg.date}" styleClass="header" />
                  </f:facet>
               </a:column>
            </a:richList>


            <c:forEach var="idx" begin="1" end="${DialogManager.bean.editionSize}">

               <a:richList id="ml-editions-list${idx}" viewMode="details" value="#{DialogManager.bean.nextSingleEditionBean.edition}" var="ed" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                  initialSortColumn="editionLabel" initialSortDescending="true">

                  <%-- Icon details view mode --%>
                  <a:column id="column-edition-view-mode${idx}" primary="true" width="20" style="padding:2px;">
                     <a:graphicImageExprEnable id="edition-image${idx}" url="/images/icons/folder.gif" />
                  </a:column>

                  <%-- edition label --%>
                  <a:column id="column-edition-label${idx}" width="100" style="text-align:left">
                     <a:outputText id="edition-label${idx}" value="#{ed.editionLabel}" />
                  </a:column>

                  <%-- edition notes --%>
                  <a:column id="column-edition-notes${idx}" width="200" style="text-align:left">
                     <a:outputText id="edition-notes${idx}" value="#{ed.editionNotes}" />
                  </a:column>

                  <%-- edition author --%>
                  <a:column id="column-edition-author${idx}" style="text-align:left">
                     <a:outputText id="edition-author${idx}" value="#{ed.editionAuthor}" />
                  </a:column>

                  <%-- edition date --%>
                  <a:column id="column-edition-date${idx}" style="text-align:left; white-space:nowrap">
                     <a:outputText id="edition-date${idx}" value="#{ed.editionDate}">
                        <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
                     </a:outputText>
                  </a:column>
               </a:richList>

               <f:verbatim>
                  <div style="padding-left: 25">
               </f:verbatim>

               <a:panel label="#{msg.related_content}" id="ml-versions-panel${idx}" progressive="true" expanded="false" expandedActionListener="#{DialogManager.bean.expandPanel}" styleClass="nodeWorkflowInfoTitle">

                  <a:richList id="ml-versions-list${idx}" viewMode="details" value="#{DialogManager.bean.currentSingleEditionBean.translations}" var="tr" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                     pageSize="10" initialSortColumn="versionName" initialSortDescending="true" style="padding-left:12px;padding-top:10px;">

                     <%-- Icon details view mode --%>
                     <a:column id="column-view-mode${idx}" primary="true" width="20" style="padding:2px;">
                        <a:graphicImageExprEnable id="translation-image${idx}" url="/images/filetypes/_default.gif" />
                     </a:column>

                     <%-- Versioned name --%>
                     <a:column id="column-name${idx}" width="100" style="text-align:left">
                        <f:facet name="header">
                           <a:sortLink id="sort-name${idx}" label="#{msg.name}" value="versionName" mode="case-insensitive" styleClass="header" />
                        </f:facet>
                        <a:actionLink id="translation-name${idx}" value="#{tr.versionName}" href="#{tr.versionUrl}" target="new" />
                     </a:column>

                     <%-- Versioned description --%>
                     <a:column id="column-description${idx}" width="170" style="text-align:left">
                        <f:facet name="header">
                           <a:outputText id="desc-title${idx}" value="#{msg.description}" styleClass="header" />
                        </f:facet>
                        <a:outputText id="translation-description${idx}" value="#{tr.versionDescription}" />
                     </a:column>

                     <%-- Versioned creation date --%>
                     <a:column id="column-creation-date${idx}" style="text-align:left; white-space:nowrap">
                        <f:facet name="header">
                           <a:sortLink id="sort-cr-date${idx}" label="#{msg.created}" value="versionCreatedDate" mode="case-insensitive" styleClass="header" />
                        </f:facet>
                        <a:outputText id="translation-created-date${idx}" value="#{tr.versionCreatedDate}">
                           <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
                        </a:outputText>
                     </a:column>

                     <%-- Versioned modified date --%>
                     <a:column id="column-modified-date${idx}" style="text-align:left; white-space:nowrap">
                        <f:facet name="header">
                           <a:sortLink id="sort-mod-date${idx}" label="#{msg.modified}" value="versionModifiedDate" mode="case-insensitive" styleClass="header" />
                        </f:facet>
                        <a:outputText id="translation-modif-date${idx}" value="#{tr.versionModifiedDate}">
                           <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
                        </a:outputText>
                     </a:column>

                     <%-- Versioned language --%>
                     <a:column id="column-language${idx}" style="text-align:left">
                        <f:facet name="header">
                           <a:sortLink id="sort-lang${idx}" label="#{msg.language}" value="versionLanguage" mode="case-insensitive" styleClass="header" />
                        </f:facet>
                        <a:outputText id="translation-language${idx}" value="#{tr.versionLanguage}" />
                     </a:column>

                     <%-- view actions --%>
                     <a:column id="column-action${idx}" style="text-align: left">
                        <f:facet name="header">
                           <a:outputText id="translation-action${idx}" value="#{msg.actions}" />
                        </f:facet>
                        <a:actionLink image="/images/icons/versioned_properties.gif" id="view-version-props${idx}" value="#{msg.properties}" showLink="false" action="showVersionedDetails" actionListener="#{VersionedDocumentDetailsDialog.setBrowsingVersion}">
                           <a:param id="param-id${idx}" name="id" value="#{DialogManager.bean.document.id}" />
                           <a:param id="param-vl${idx}" name="versionLabel" value="#{ed.editionLabel}" />
                           <a:param id="param-lg${idx}" name="lang" value="#{tr.versionLanguage}" />
                        </a:actionLink>
                     </a:column>
                  </a:richList>
               </a:panel>
               <f:verbatim>
                  </div>
               </f:verbatim>
            </c:forEach>
         </a:panel> <f:verbatim>
            <td valign="top">
         </f:verbatim> 
         <%-- Document Actions --%> 
         <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true" expanded='#{DialogManager.bean.panels["actions-panel"]}'
            expandedActionListener="#{DialogManager.bean.expandPanel}">
            <r:actions id="actions_doc" value="multilingual_details_actions" context="#{DialogManager.bean.document}" verticalSpacing="3" style="white-space:nowrap" />
         </a:panel><f:verbatim>
         </td>
      </tr>
   </table>
</f:verbatim>
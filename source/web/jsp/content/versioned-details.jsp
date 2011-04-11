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

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="javax.faces.context.FacesContext" %>

<r:page titleId="title_versioned_doc_details">

<f:view>
   <%
      FacesContext fc = FacesContext.getCurrentInstance();

      // set locale for JSF framework usage
      fc.getViewRoot().setLocale(Application.getLanguage(fc));
   %>

   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>

   <h:form acceptcharset="UTF-8" id="versioned-document-details">

   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2" width="100%">

      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>

      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>

         <%-- Work Area --%>
         <td width="<h:outputText value="#{NavigationBean.workAreaWidth}" />">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>

               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#dfe6ed">

                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <img src="<%=request.getContextPath()%>/images/icons/details_large.gif" width=32 height=32>
                           </td>
                           <td>
                              <div class="mainTitle">
                                  <h:outputText value="#{msg.versioned_details_of}" /> <h:outputText value="#{msg.left_qoute}"/><h:outputText value="#{VersionedDocumentDetailsDialog.name}" /><h:outputText value="#{msg.right_quote}"/>
                              </div>
                              <div class="mainSubText">
                                  <h:outputText value="#{msg.version}" />: <h:outputText value="#{msg.left_qoute}"/><h:outputText value="#{VersionedDocumentDetailsDialog.version.versionLabel}" /><h:outputText value="#{msg.right_quote}"/>
                              </div>
                              <div class="mainSubText"><h:outputText id="doc-details" value="#{msg.versioned_documentdetails_description}" /></div>
                           </td>

                           <%-- Navigation --%>
                           <td align=right>
                              <a:actionLink id="act-prev" value="#{msg.previous_item}" verticalAlign="-8px" image="/images/icons/nav_prev.gif" showLink="false" actionListener="#{VersionedDocumentDetailsDialog.previousItem}" action="showVersionedDetails"/>
                              <img src="<%=request.getContextPath()%>/images/icons/nav_file.gif" width=24 height=24 align=absmiddle>
                              <a:actionLink id="act-next" value="#{msg.next_item}" verticalAlign="-8px" image="/images/icons/nav_next.gif" showLink="false" actionListener="#{VersionedDocumentDetailsDialog.nextItem}" action="showVersionedDetails"/>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
               </tr>

               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
               </tr>

               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <a:errors message="" infoClass="statusWarningText" errorClass="statusErrorText" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>

               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">

                              <h:panelGroup id="props-panel-facets">
                                 <f:facet name="title"/>
                              </h:panelGroup>
                              <a:panel label="#{msg.properties}" id="properties-panel" facetsId="props-panel-facets" progressive="true"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
                                       expanded="true">
                                 <table cellspacing="0" cellpadding="0" border="0" width="100%">
                                    <tr>
                                       <td width=80 align=center>
                                          <%-- icon image for the doc --%>
                                          <table cellspacing=0 cellpadding=0 border=0>
                                             <tr>
                                                <td>
                                                   <div style="border: thin solid #CCCCCC; padding:4px">
                                                      <a:actionLink id="doc-logo1-not-empty" value="#{VersionedDocumentDetailsDialog.name}" href="#{VersionedDocumentDetailsDialog.url}" target="new" image="#{VersionedDocumentDetailsDialog.fileType32}" showLink="false" rendered="#{VersionedDocumentDetailsDialog.emptyTranslation == false}"/>
                                                      <h:graphicImage id="doc-logo1-empty" value="#{VersionedDocumentDetailsDialog.fileType32}" rendered="#{VersionedDocumentDetailsDialog.emptyTranslation == true}"/>
                                                   </div>
                                                </td>
                                                <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width=6 height=42></td>
                                             </tr>
                                             <tr>
                                                <td colspan=2><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width=48 height=5></td>
                                             </tr>
                                          </table>
                                       </td>
                                       <td>
                                          <%-- properties for the doc --%>
                                          <r:propertySheetGrid id="document-props" value="#{VersionedDocumentDetailsDialog.frozenStateDocument}" var="documentProps"
                                                      columns="1" mode="view" labelStyleClass="propertiesLabel"
                                                      externalConfig="true" />
                                          <h:message id="msg1" for="document-props" styleClass="statusMessage" />
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>

                              <div style="padding:4px"></div>

                              <%-- Multilingual properties --%>
                              <h:panelGroup id="ml-props-panel-facets">
                                 <f:facet name="title"/>
                              </h:panelGroup>

                              <%-- Panel if the node has the multilingual aspect--%>
                              <a:panel label="#{msg.ml_content_info}" facetsId="ml-props-panel-facets" id="ml-info-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" rendered="#{VersionedDocumentDetailsDialog.fromPreviousEditon}"
                                       expanded='true' >

                                 <%-- properties for Ml container --%>
                                 <div style="padding:4px"></div>
                                 <h:outputText value="#{msg.properties}" styleClass="nodeWorkflowInfoTitle" style="padding:20px;"/>
                                 <div style="padding:4px"></div>

                                 <r:propertySheetGrid id="ml-container-props-sheet" value="#{VersionedDocumentDetailsDialog.multilingualContainerDocument}"
                                          var="mlContainerProps" columns="1" labelStyleClass="propertiesLabel"
                                          externalConfig="true" cellpadding="2" cellspacing="2" mode="view"/>

                                 <div style="padding:8px"></div>

                                 <a:panel label="#{msg.related_translations}" id="related-translation-panel" progressive="true" expanded='false' >

                                    <div style="padding:4px"></div>
                                    <%-- list of translations --%>
                                    <a:richList id="TranslationList" viewMode="details" value="#{VersionedDocumentDetailsDialog.translations}"
                                               var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
                                               rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                               pageSize="10" initialSortColumn="Name" initialSortDescending="false">

                                       <%-- Name and icon columns --%>
                                       <a:column id="col21" primary="true" width="300" style="text-align:left">
                                          <f:facet name="small-icon">
                                             <h:graphicImage id="gfx0" url="/images/filetypes/_default.gif" width="16" height="16"/>
                                          </f:facet>
                                          <f:facet name="header">
                                             <a:sortLink id="sort0" label="#{msg.name}" value="Name" mode="case-insensitive" styleClass="header"/>
                                          </f:facet>
                                          <a:actionLink id="view-name" value="#{r.name}" href="#{r.url}" target="new" />
                                       </a:column>

                                       <%-- Language columns --%>
                                       <a:column id="col22" width="50" style="text-align:left">
                                          <f:facet name="header">
                                             <a:sortLink id="sort1" label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header"/>
                                          </f:facet>
                                          <h:outputText id="view-language" value="#{r.language}"/>
                                       </a:column>

                                       <%-- view actions --%>
                                       <a:column id="col25" style="text-align: left">
                                          <f:facet name="header">
                                             <h:outputText id="txt1" value="#{msg.actions}"/>
                                          </f:facet>
                                          <a:actionLink id="view-link1" value="#{msg.view}" href="#{r.url}" target="new" />
                                       </a:column>

                                       <a:dataPager id="pager0" styleClass="pager" />
                                    </a:richList>
                                 </a:panel>
                              </a:panel>

                              <div style="padding:4px"></div>

                              <a:panel label="#{msg.version_history}" id="version-history-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" rendered="#{VersionedDocumentDetailsDialog.fromPreviousEditon}"
                                       expanded="true">

                                 <a:richList id="versionHistoryList" viewMode="details" value="#{VersionedDocumentDetailsDialog.versionHistory}"
                                             var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
                                             rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                             pageSize="10" initialSortColumn="versionDate" initialSortDescending="true">

                                    <%-- Primary column for details view mode --%>
                                    <a:column id="col1" primary="true" width="100" style="padding:2px;text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink id="sort2" label="#{msg.version}" value="versionLabel" mode="case-insensitive" styleClass="header"/>
                                       </f:facet>
                                       <a:actionLink id="label-link" value="#{r.versionLabel}" href="#{r.url}" target="new" rendered="#{r.url != null}"/>
                                       <a:actionLink id="label-no-link" value="#{r.versionLabel}"  rendered="#{r.url == null}"/>
                                    </a:column>

                                    <%-- Version notes columns --%>
                                    <a:column id="col2" width="170" style="text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink id="sort3" label="#{msg.notes}" value="notes" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="notes" value="#{r.notes}" />
                                    </a:column>

                                    <%-- Description columns --%>
                                    <a:column id="col3" style="text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink id="sort4" label="#{msg.author}" value="author" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="author" value="#{r.author}" />
                                    </a:column>

                                    <%-- Created Date column for details view mode --%>
                                    <a:column id="col4" style="text-align:left; white-space:nowrap">
                                       <f:facet name="header">
                                          <a:sortLink id="sort5" label="#{msg.date}" value="versionDate" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="date" value="#{r.versionDate}">
                                          <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                                       </h:outputText>
                                    </a:column>

                                    <%-- view the contents of the specific version --%>
                                    <a:column id="col5" style="text-align: left">
                                       <f:facet name="header">
                                          <h:outputText id="txt2" value="#{msg.actions}"/>
                                       </f:facet>
                                       <a:actionLink id="view-version-props" value="#{msg.properties}" actionListener="#{VersionedDocumentDetailsDialog.nextItem}" action="showVersionedDetails">
                                           <f:param id="view-version-props-versionLabel" name="versionLabel" value="#{r.versionLabel}" />
                                       </a:actionLink>

                                       <h:outputText id="space" value=" " />
                                       <a:actionLink id="view-link2" value="#{msg.view}" href="#{r.url}" target="new" rendered="#{r.url != null}"/>

                                    </a:column>

                                    <a:dataPager id="pager1" styleClass="pager" />
                                 </a:richList>
                              </a:panel>
                           </td>

                           <td valign="top">

                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0" width="100%">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="close-btn-mlc-details" value="#{msg.close}" action="dialog:close:dialog:showMLContainerDetails" styleClass="wizardButton"  rendered="#{VersionedDocumentDetailsDialog.fromPreviousEditon == true}" />
                                       <h:commandButton id="close-btn-doc-details" value="#{msg.close}" action="dialog:close:dialog:showDocDetails" styleClass="wizardButton"  rendered="#{VersionedDocumentDetailsDialog.fromPreviousEditon == false}" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>

                              <div style="padding:4px"></div>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>

               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
               </tr>

            </table>
          </td>
       </tr>
    </table>

    </h:form>

</f:view>

</r:page>
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

<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="javax.faces.context.FacesContext" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="100kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<r:page titleId="title_browse">

<script type="text/javascript">
   function applySizeSpaces(e)
   {
      return applySize(e, 'spaces-apply');
   }
   
   function applySizeContent(e)
   {
      return applySize(e, 'content-apply');
   }
   
   function applySize(e, field)
   {
      var keycode;
      if (window.event) keycode = window.event.keyCode;
      else if (e) keycode = e.which;
      if (keycode == 13)
      {
         document.forms['browse']['browse:act'].value='browse:' + field;
         document.forms['browse'].submit();
         return false;
      }
      return true;
   }
</script>

<f:view>
   <%
      FacesContext fc = FacesContext.getCurrentInstance();
     
      // set locale for JSF framework usage
      fc.getViewRoot().setLocale(Application.getLanguage(fc));
   %>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="browse">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2" width="100%">

      <%-- Title bar --%>
      <tr>
         <td colspan=2>
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td style="white-space: nowrap;">
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="<h:outputText value="#{NavigationBean.workAreaWidth}" />">
            <table cellspacing=0 cellpadding=0 width='100%'>
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width=4></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width='100%'>
                        <tr>
 
                           <%-- actions for browse mode --%>
                           <a:panel id="browse-actions" rendered="#{NavigationBean.searchContext == null}">
                              <td width=32>
                                 <h:graphicImage id="space-logo" url="/images/icons/#{NavigationBean.nodeProperties.icon}.gif" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle">
                                    <h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" />&nbsp;
                                    <a:actionLink image="/images/icons/opennetwork.gif" value="#{msg.network_folder} #{NavigationBean.nodeProperties.cifsPathLabel}" showLink="false" href="#{NavigationBean.nodeProperties.cifsPath}" rendered="#{NavigationBean.nodeProperties.cifsPath != null}" target="new" id="cifs" />&nbsp;
                                    <a:actionLink id="actRSS" value="#{msg.rss_feed_link}" showLink="false" image="/images/icons/rss.gif" href="#{NavigationBean.RSSFeedURL}" rendered="#{NavigationBean.RSSFeed == true}" />
                                 </div>
                                 <div class="mainSubText"><h:outputText value="#{msg.view_description}" id="msg3" /></div>
                                 <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                              </td>
                              <td style="padding-right:4px" align=right>
                                 <nobr>
                                 <%-- Additional summary info --%>
                                 <h:graphicImage id="img-rule" url="/images/icons/rule.gif" width="16" height="16" title="#{msg.rules_count}" /> <h:outputText value="(#{NavigationBean.ruleCount})" id="rulemsg1" style="vertical-align:20%" />
                                 </nobr>
                              </td>
                              <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                              <td style="padding-left:4px; white-space:nowrap" align="right">
                                 <%-- Quick upload action --%>
                                <r:actions id="acts_add_content" value="add_content_menu" context="#{NavigationBean.currentNode}" showLink="true" />
                              </td>
                              <td style="padding-left:4px" width=52>
                                 <%-- Create actions menu --%>
                                 <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap" rendered="#{NavigationBean.createChildrenPermissionEnabled}">
                                    <r:actions id="acts_create" value="browse_create_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                              <td style="padding-left:4px" width=80>
                                 <%-- More actions menu --%>
                                 <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="acts_browse" value="browse_actions_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                           </a:panel>
                           
                           <%-- actions for search results mode --%>
                           <a:panel id="search-actions" rendered="#{NavigationBean.searchContext != null}">
                              <td width=32>
                                 <img src="<%=request.getContextPath()%>/images/icons/search_results_large.gif" width=32 height=32>
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{msg.search_results}" id="msg11" /></div>
                                 <div class="mainSubText"><h:outputFormat value="#{msg.search_detail}" id="msg12"><f:param value="#{NavigationBean.searchContext.text}" id="param2" /></h:outputFormat></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.search_description}" id="msg13" /></div>
                              </td>
                              <td style="padding-right:4px" align=right>
                                 <%-- Close Search action --%>
                                 <nobr><a:actionLink value="#{msg.close_search}" image="/images/icons/action.gif" style="white-space:nowrap" actionListener="#{BrowseBean.closeSearch}" id="link21" /></nobr>
                              </td>
                              <td style="padding-right:4px" width=80>
                                 <%-- New Search actions --%>
                                 <nobr><a:actionLink value="#{msg.new_search}" image="/images/icons/search_icon.gif" style="white-space:nowrap" action="advSearch" id="link20" /></nobr>
                              </td>
                              <td style="padding-left:4px" width=90>
                                 <%-- More Search actions --%>
                                 <a:menu id="searchMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <a:booleanEvaluator value="#{NavigationBean.isGuest == false}" id="eval0">
                                       <a:actionLink value="#{msg.save_new_search}" image="/images/icons/save_search.gif" padding="4" action="#{AdvancedSearchDialog.saveNewSearch}" id="link20_1" />
                                       <a:booleanEvaluator value="#{AdvancedSearchDialog.allowEdit == true}" id="eval0_1">
                                          <a:actionLink value="#{msg.save_edit_search}" image="/images/icons/edit_search.gif" padding="4" action="#{AdvancedSearchDialog.saveEditSearch}" id="link20_2" />
                                       </a:booleanEvaluator>
                                    </a:booleanEvaluator>
                                 </a:menu>
                              </td>
                           </a:panel>
                           
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                           <td width=118 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList id="viewMode" itemSpacing="4" iconColumnWidth="20" selectedStyleClass="statusListHighlight" disabledStyleClass="statusListDisabled" selectedImage="/images/icons/Details.gif"
                                    value="#{BrowseBean.browseViewMode}" actionListener="#{BrowseBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="details" label="#{msg.details_view}" />
                                 <a:listItem value="icons" label="#{msg.view_icon}" />
                                 <a:listItem value="list" label="#{msg.view_browse}" />
                                 <a:listItem value="dashboard" label="#{msg.custom_view}" disabled="#{!NavigationBean.hasCustomView}" />
                              </a:modeList>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width=4 height=9></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width=4 height=9></td>
               </tr>

               <%-- warning message for 'Sites' space --%>
               <h:outputText id="sites-space-warning" rendered="#{BrowseBean.sitesSpace}" value="#{BrowseBean.sitesSpaceWarningHTML}" escape="false" />
               
               <%-- Custom Template View --%>
               <a:panel id="custom-wrapper-panel" rendered="#{NavigationBean.hasCustomView && NavigationBean.searchContext == null}">
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     <a:panel id="custom-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              label="#{msg.custom_view}" progressive="true"
                              expanded='#{BrowseBean.panels["custom-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                        <r:webScript id="webscript" scriptUrl="#{NavigationBean.currentNodeWebscript}" context="#{NavigationBean.currentNode.nodeRef}" rendered="#{NavigationBean.hasWebscriptView}" />
                        <r:template id="template" template="#{NavigationBean.currentNodeTemplate}" model="#{NavigationBean.templateModel}" rendered="#{!NavigationBean.hasWebscriptView && NavigationBean.hasTemplateView}" />
                     </a:panel>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               </a:panel>
               
               <%-- Details - Spaces --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <%-- wrapper comment used by the panel to add additional component facets --%>
                     <h:panelGroup id="spaces-panel-facets">
                        <f:facet name="title">
                           <a:panel id="page-controls1" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="items-txt1"/>
                              <h:inputText id="spaces-pages" value="#{BrowseBean.pageSizeSpacesStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeSpaces(event);" />
                              <f:verbatim><div style="display:none"></f:verbatim>
                              <a:actionLink id="spaces-apply" value="" actionListener="#{BrowseBean.updateSpacesPageSize}" />
                              <f:verbatim></div></f:verbatim>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="spaces-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              label="#{msg.browse_spaces}" progressive="true" facetsId="spaces-panel-facets"
                              expanded='#{BrowseBean.panels["spaces-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                     
                     <%-- Spaces List --%>
                     <a:richList id="spacesList" binding="#{BrowseBean.spacesRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.pageSizeSpaces}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.nodes}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <%-- TODO: either build complete message in BrowseBean or have no icon... --%>
                           <h:outputFormat id="no-space-items" value="#{msg.no_space_items}" escape="false" rendered="#{NavigationBean.searchContext == null}">
                              <f:param id="param-cp" value="#{msg.create_space}" />
                           </h:outputFormat>
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column id="col1" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink id="col1-act1" value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" id="param3" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink id="col1-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
                              <f:param name="id" value="#{r.id}" id="param4" />
                           </a:actionLink>
                           <r:nodeInfo id="col1-info" value="#{r.id}">
                              <h:graphicImage id="col1-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Primary column for icons view mode --%>
                        <a:column id="col2" primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="large-icon">
                              <a:actionLink id="col2-act1" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" id="param5" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink id="col2-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="header">
                              <f:param name="id" value="#{r.id}" id="param6" />
                           </a:actionLink>
                           <r:nodeInfo id="col2-info" value="#{r.id}">
                              <h:graphicImage id="col2-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Primary column for list view mode --%>
                        <a:column id="col3" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <f:facet name="large-icon">
                              <a:actionLink id="col3-act1" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" id="param7" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink id="col3-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="title">
                              <f:param name="id" value="#{r.id}" id="param8" />
                           </a:actionLink>
                           <r:nodeInfo id="col3-info" value="#{r.id}">
                              <h:graphicImage id="col3-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Description column for all view modes --%>
                        <a:column id="col4" style="text-align:left">
                           <f:facet name="header">
                              <a:sortLink id="col4-sort" label="#{msg.description}" value="description" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col4-txt" value="#{r.description}" />
                        </a:column>
                        
                        <%-- Path column for search mode in details view mode --%>
                        <a:column id="col5" style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col5-sort" label="#{msg.path}" value="displayPath" styleClass="header"/>
                           </f:facet>
                           <r:nodePath id="col5-path" value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                        </a:column>
                        
                        <%-- Created Date column for details view mode --%>
                        <a:column id="col6" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col6-sort" label="#{msg.created}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col6-txt" value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Modified Date column for details/icons view modes --%>
                        <a:column id="col7" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink id="col7-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col7-txt" value="#{r.modified}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Node Descendants links for list view mode --%>
                        <a:column id="col8" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <r:nodeDescendants id="col8-kids" value="#{r.nodeRef}" styleClass="header" actionListener="#{BrowseBean.clickDescendantSpace}" />
                        </a:column>
                        
                        <%-- Space Actions column --%>
                        <a:column id="col9" actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText id="col9-txt" value="#{msg.actions}"/>
                           </f:facet>
                           
                           <%-- actions are configured in web-client-config-actions.xml --%>
                           <r:actions id="col9-acts1" value="space_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           
                           <%-- More actions menu --%>
                           <a:menu id="spaces-more-menu" itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                              <r:actions id="col9-acts2" value="space_browse_menu" context="#{r}" />
                           </a:menu>
                        </a:column>
                        
                        <a:dataPager id="pager1" styleClass="pager" />
                     </a:richList>
                     
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Content --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <h:panelGroup id="content-panel-facets">
                        <f:facet name="title">
                           <a:panel id="page-controls2" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="items-txt2"/>
                              <h:inputText id="content-pages" value="#{BrowseBean.pageSizeContentStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeContent(event);" />
                              <f:verbatim><div style="display:none"></f:verbatim>
                              <a:actionLink id="content-apply" value="" actionListener="#{BrowseBean.updateContentPageSize}" />
                              <f:verbatim></div></f:verbatim>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="content-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                           label="#{msg.browse_content}" progressive="true" facetsId="content-panel-facets"
                           expanded='#{BrowseBean.panels["content-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                     
                     <%-- Content list --%>
                     <a:richList id="contentRichList" binding="#{BrowseBean.contentRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.pageSizeContent}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.content}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <%-- TODO: either build complete message in BrowseBean or have no icon... --%>
                           <h:outputFormat id="no-content-items" value="#{msg.no_content_items}" escape="false" rendered="#{NavigationBean.searchContext == null}">
                              <f:param value="#{msg.add_content}" id="param10" />
                              <f:param value="#{msg.create_content}" id="param11" />
                           </h:outputFormat>
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column id="col10" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col10-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink id="col10-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink id="col10-act2" value="#{r.name}" href="#{r.url}" target="new" />
                           <r:lockIcon id="col10-lock" value="#{r.nodeRef}" align="absmiddle" />
                           <h:outputLabel id="col10-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}" />
                           <r:nodeInfo id="col10-info" value="#{r.id}">
                              <h:graphicImage id="col10-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Primary column for icons view mode --%>
                        <a:column id="col11" primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="large-icon">
                              <a:actionLink id="col11-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink id="col11-act2" value="#{r.name}" href="#{r.url}" target="new" styleClass="header" />
                           <r:lockIcon id="col11-lock" value="#{r.nodeRef}" align="absmiddle" />
                           <h:outputLabel id="col11-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}"/>
                           <r:nodeInfo id="col11-info" value="#{r.id}">
                              <h:graphicImage id="col11-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Primary column for list view mode --%>
                        <a:column id="col12" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <f:facet name="large-icon">
                              <a:actionLink id="col12-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink id="col12-act2" value="#{r.name}" href="#{r.url}" target="new" styleClass="title" />
                           <r:lockIcon id="col12-lock" value="#{r.nodeRef}" align="absmiddle" />
                           <h:outputLabel id="col12-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}"/>
                           <r:nodeInfo id="col12-info" value="#{r.id}">
                              <h:graphicImage id="col12-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                           </r:nodeInfo>
                        </a:column>
                        
                        <%-- Description column for all view modes --%>
                        <a:column id="col13" style="text-align:left">
                           <f:facet name="header">
                              <a:sortLink id="col13-sort" label="#{msg.description}" value="description" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col13-txt" value="#{r.description}" />
                        </a:column>
                        
                        <%-- Path column for search mode in details view mode --%>
                        <a:column id="col14" style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col14-sort" label="#{msg.path}" value="displayPath" styleClass="header"/>
                           </f:facet>
                           <r:nodePath id="col14-path" value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                        </a:column>
                        
                        <%-- Size for details/icons view modes --%>
                        <a:column id="col15" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink id="col15-sort" label="#{msg.size}" value="size" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col15-txt" value="#{r.size}">
                              <a:convertSize />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Created Date column for details view mode --%>
                        <a:column id="col16" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink id="col16-sort" label="#{msg.created}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col16-txt" value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Modified Date column for details/icons view modes --%>
                        <a:column id="col17" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink id="col17-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
                           </f:facet>
                           <h:outputText id="col17-txt" value="#{r.modified}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Content Actions column --%>
                        <a:column id="col18" actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText id="col18-txt" value="#{msg.actions}"/>
                           </f:facet>
                           
                           <%-- actions are configured in web-client-config-actions.xml --%>
                           <r:actions id="col18-acts1" value="document_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           
                           <%-- More actions menu --%>
                           <a:menu id="content-more-menu" itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                              <r:actions id="col18-acts2" value="document_browse_menu" context="#{r}" />
                           </a:menu>
                        </a:column>
                        
                        <a:dataPager id="pager2" styleClass="pager" />
                     </a:richList>
                     
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     <%-- messages tag to show messages not handled by other specific message tags --%> 
                     <a:errors message="" infoClass="statusWarningText" errorClass="statusErrorText" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width=4 height=4></td>
                  <td width='100%' align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>

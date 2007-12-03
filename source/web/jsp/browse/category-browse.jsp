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
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="javax.faces.context.FacesContext" %>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="100kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<r:page titleId="title_category_browse">

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
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

<h:form acceptcharset="UTF-8" id="category-browse">

   <%-- Main outer table --%>
   <table cellspacing=0 cellpadding=2>
   
      <%-- Title bar --%>
      <tr>
         <td colspan=2>
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign=top>
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width=100%>
            <table cellspacing=0 cellpadding=0 width=100%>
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width=4></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width=100%>
                        <tr>
                           <%-- actions --%>
                           <a:panel id="category-search-actions" rendered="#{NavigationBean.searchContext != null}">
                              <td width=32>
                                 <img src="<%=request.getContextPath()%>/images/icons/search_results_large.gif" width=32 height=32>
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputFormat value="#{msg.category_browser_browse_title}" id="category-msg11"><f:param value="#{CategoryBrowserBean.currentCategoryName}" id="category-param2" /></h:outputFormat><h:outputLabel id="category-msg121" value=" #{msg.category_browser_browse_include}" rendered="#{CategoryBrowserBean.includeSubcategories}"/></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.category_browser_browse_description}" id="category-msg13" /><h:outputLabel id="category-msg131" value=" #{msg.category_browser_browse_include}" rendered="#{CategoryBrowserBean.includeSubcategories}"/></div>
                              </td>
                           </a:panel>
                           
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                           <td width=118 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList id="category-viewMode" itemSpacing="4" iconColumnWidth="20" selectedStyleClass="statusListHighlight" disabledStyleClass="statusListDisabled" selectedImage="/images/icons/Details.gif"
                                    value="#{BrowseBean.browseViewMode}" actionListener="#{BrowseBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="details" label="#{msg.details_view}" />
                                 <a:listItem value="icons" label="#{msg.view_icon}" />
                                 <a:listItem value="list" label="#{msg.view_browse}" />
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
               
               <%-- Custom Template View --%>
               <a:panel id="category-custom-wrapper-panel" rendered="#{NavigationBean.hasCustomView}">
                  <tr valign=top>
                     <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                     <td style="padding:4px">
                        <a:panel id="category-custom-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                           label="#{msg.custom_view}" progressive="true"
                           expanded='#{BrowseBean.panels["custom-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                           <r:webScript id="category-webscript" scriptUrl="#{NavigationBean.currentNodeWebscript}" context="#{NavigationBean.currentNode.nodeRef}" rendered="#{NavigationBean.hasWebscriptView}" />
                           <r:template id="category-template" template="#{NavigationBean.currentNodeTemplate}" model="#{NavigationBean.templateModel}" rendered="#{!NavigationBean.hasWebscriptView && NavigationBean.hasTemplateView}" />
                        </a:panel>
                     </td>
                     <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
                  </tr>
               </a:panel>
               
               <%-- Details - Spaces --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                  
                     <%-- wrapper comment used by the panel to add additional component facets --%>
                     <h:panelGroup id="category-spaces-panel-facets">
                        <f:facet name="title">
                           <a:panel id="category-page-controls1" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="category-items-txt1"/>
                              <h:inputText id="category-spaces-pages" value="#{BrowseBean.pageSizeSpacesStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeSpaces(event);" />
                              <div style="display:none"><a:actionLink id="category-spaces-apply" value="" actionListener="#{BrowseBean.updateSpacesPageSize}" /></div>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="category-spaces-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                        label="#{msg.browse_spaces}" progressive="true" facetsId="category-spaces-panel-facets"
                        expanded='#{BrowseBean.panels["spaces-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                        
                        <%-- Spaces List --%>
                        <a:richList id="category-spacesList" binding="#{BrowseBean.spacesRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.pageSizeSpaces}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.nodes}" var="r">
                           
                           <%-- Primary column for details view mode --%>
                           <a:column id="category-col1" primary="true" width="200" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="category-col1-act1" value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </f:facet>
                              <a:actionLink id="category-col1-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
                              <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              <r:nodeInfo id="category-col1-info" value="#{r.id}">
                                 <h:graphicImage id="category-col1-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Primary column for icons view mode --%>
                           <a:column id="category-col2" primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                              <f:facet name="large-icon">
                                 <a:actionLink id="category-col2-act1" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </f:facet>
                              <a:actionLink id="category-col2-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="header">
                              <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              <r:nodeInfo id="category-col2-info" value="#{r.id}">
                                 <h:graphicImage id="category-col2-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Primary column for list view mode --%>
                           <a:column id="category-col3" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                              <f:facet name="large-icon">
                                 <a:actionLink id="category-col3-act1" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </f:facet>
                              <a:actionLink id="category-col3-act2" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="title">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                              <r:nodeInfo id="category-col3-info" value="#{r.id}">
                                 <h:graphicImage id="category-col3-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Description column for all view modes --%>
                           <a:column id="category-col4" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="category-col4-sort" label="#{msg.description}" value="description" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col4-txt" value="#{r.description}" />
                           </a:column>
                           
                           <%-- Path column for search mode in details view mode --%>
                           <a:column id="category-col5" style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col5-sort" label="#{msg.path}" value="displayPath" styleClass="header"/>
                              </f:facet>
                              <r:nodePath id="category-col5-path" value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                           </a:column>
                           
                           <%-- Created Date column for details view mode --%>
                           <a:column id="category-col6" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col6-sort" label="#{msg.created}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col6-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modified Date column for details/icons view modes --%>
                           <a:column id="category-col7" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col7-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col7-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Node Descendants links for list view mode --%>
                           <a:column id="category-col8" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                              <r:nodeDescendants id="category-col8-kids" value="#{r.nodeRef}" styleClass="header" actionListener="#{BrowseBean.clickDescendantSpace}" />
                           </a:column>
                           
                           <%-- Space Actions column --%>
                           <a:column id="category-col9" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="category-col9-txt" value="#{msg.actions}"/>
                              </f:facet>
                           
                              <%-- actions are configured in web-client-config-actions.xml --%>
                              <r:actions id="category-col9-acts1" value="space_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                              
                              <%-- More actions menu --%>
                              <a:menu id="category-spaces-more-menu" itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                                 <r:actions id="category-col9-acts2" value="space_browse_menu" context="#{r}" />
                              </a:menu>
                           </a:column>
                           
                           <a:dataPager id="category-pager1" styleClass="pager" />
                        </a:richList>
                     
                     </a:panel>
                  
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
                  
               <%-- Details - Content --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                  
                     <h:panelGroup id="category-content-panel-facets">
                        <f:facet name="title">
                           <a:panel id="category-page-controls2" style="font-size:9px">
                              <h:outputText value="#{msg.items_per_page}" id="category-items-txt2"/>
                              <h:inputText id="category-content-pages" value="#{BrowseBean.pageSizeContentStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeContent(event);" />
                              <div style="display:none"><a:actionLink id="category-content-apply" value="" actionListener="#{BrowseBean.updateContentPageSize}" /></div>
                           </a:panel>
                        </f:facet>
                     </h:panelGroup>
                     <a:panel id="category-content-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                        label="#{msg.browse_content}" progressive="true" facetsId="category-content-panel-facets"
                        expanded='#{BrowseBean.panels["content-panel"]}' expandedActionListener="#{BrowseBean.expandPanel}">
                     
                        <%-- Content list --%>
                        <a:richList id="category-contentRichList" binding="#{BrowseBean.contentRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.pageSizeContent}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.content}" var="r">
                           
                           <%-- Primary column for details view mode --%>
                           <a:column id="category-col10" primary="true" width="200" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col10-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="category-col10-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
                              </f:facet>
                              <a:actionLink id="category-col10-act2" value="#{r.name}" href="#{r.url}" target="new" />
                              <r:lockIcon id="category-col10-lock" value="#{r.nodeRef}" align="absmiddle" />
                              <h:outputLabel id="category-col10-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}" />
                              <r:nodeInfo id="category-col10-info" value="#{r.id}">
                                 <h:graphicImage id="category-col10-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Primary column for icons view mode --%>
                           <a:column id="category-col11" primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                              <f:facet name="large-icon">
                                 <a:actionLink id="category-col11-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                              </f:facet>
                              <a:actionLink id="category-col11-act2" value="#{r.name}" href="#{r.url}" target="new" styleClass="header" />
                              <r:lockIcon id="category-col11-lock" value="#{r.nodeRef}" align="absmiddle" />
                              <h:outputLabel id="category-col11-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}"/>
                              <r:nodeInfo id="category-col11-info" value="#{r.id}">
                                 <h:graphicImage id="category-col11-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Primary column for list view mode --%>
                           <a:column id="category-col12" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                              <f:facet name="large-icon">
                                 <a:actionLink id="category-col12-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                              </f:facet>
                              <a:actionLink id="category-col12-act2" value="#{r.name}" href="#{r.url}" target="new" styleClass="title" />
                              <r:lockIcon id="category-col12-lock" value="#{r.nodeRef}" align="absmiddle" />
                              <h:outputLabel id="category-col12-lang" value="#{r.lang}" styleClass="langCode" rendered="#{r.lang != null}"/>
                              <r:nodeInfo id="category-col12-info" value="#{r.id}">
                                 <h:graphicImage id="category-col12-img" url="/images/icons/popup.gif" styleClass="popupImage" width="16" height="16" />
                              </r:nodeInfo>
                           </a:column>
                           
                           <%-- Description column for all view modes --%>
                           <a:column id="category-col13" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink  id="category-col13-sort" label="#{msg.description}" value="description" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col13-txt" value="#{r.description}" />
                           </a:column>
                           
                           <%-- Path column for search mode in details view mode --%>
                           <a:column id="category-col14" style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col14-sort" label="#{msg.path}" value="displayPath" styleClass="header"/>
                              </f:facet>
                              <r:nodePath id="category-col14-path" value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                           </a:column>
                           
                           <%-- Size for details/icons view modes --%>
                           <a:column id="category-col15" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col15-sort" label="#{msg.size}" value="size" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col15-txt" value="#{r.size}">
                                 <a:convertSize />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Created Date column for details view mode --%>
                           <a:column id="category-col16" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col16-sort" label="#{msg.created}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col16-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modified Date column for details/icons view modes --%>
                           <a:column id="category-col17" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                              <f:facet name="header">
                                 <a:sortLink id="category-col17-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="category-col17-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Content Actions column --%>
                           <a:column id="category-col18" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="category-col18-txt" value="#{msg.actions}"/>
                              </f:facet>
                              
                              <%-- actions are configured in web-client-config-actions.xml --%>
                              <r:actions id="category-col18-acts1" value="document_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                              
                              <%-- More actions menu --%>
                              <a:menu id="category-content-more-menu" itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                                 <r:actions id="category-col18-acts2" value="document_browse_menu" context="#{r}" />
                              </a:menu>
                           </a:column>
                           
                           <a:dataPager id="category-pager2" styleClass="pager" />
                        </a:richList>
                     
                     </a:panel>
                  
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign=top>
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
                  <td width=100% align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
               </tr>
            
            </table>
         </td>
      </tr>
   </table>

</h:form>

</f:view>

</r:page>
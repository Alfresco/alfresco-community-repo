<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
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

<r:page titleId="title_browse">

<f:view>
   <%
      FacesContext fc = FacesContext.getCurrentInstance();
     
      // set locale for JSF framework usage
      fc.getViewRoot().setLocale(Application.getLanguage(fc));
   %>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="browse">
   
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
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width=100%>
                        <tr>
 
                           <%-- actions for browse mode --%>
                           <a:panel id="browse-actions" rendered="#{NavigationBean.searchContext == null}">
                              <td width=32>
                                 <h:graphicImage id="space-logo" url="/images/icons/#{NavigationBean.nodeProperties.icon}.gif" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" />&nbsp;<a:actionLink image="/images/icons/opennetwork.gif" value="#{msg.network_folder} #{NavigationBean.nodeProperties.cifsPathLabel}" showLink="false" href="#{NavigationBean.nodeProperties.cifsPath}" rendered="#{NavigationBean.nodeProperties.cifsPath != null}" target="new" id="cifs" /></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.view_description}" id="msg3" /></div>
                                 <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                              </td>
                              <td style="padding-right:4px" align=right>
                                 <nobr>
                                 <%-- Additional summary info --%>
                                 <h:graphicImage id="img-rule" url="/images/icons/rule.gif" width="16" height="16" title="#{msg.rules_count}" /> <h:outputText value="(#{NavigationBean.ruleCount})" id="rulemsg1" style="vertical-align:20%" />
                                 </nobr>
                              </td>
                              <td class="separator" width=1></td>
                              <td style="padding-left:4px" align=right>
                                 <%-- Quick upload action --%>
                                 <nobr>
                                 <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren" id="eval2">
                                    <a:actionLink value="#{msg.add_content}" image="/images/icons/add.gif" padding="2" action="addContent" actionListener="#{AddContentWizard.startWizard}" style="white-space:nowrap" id="link3" />
                                 </r:permissionEvaluator>
                                 </nobr>
                              </td>
                              <td style="padding-left:4px" width=52>
                                 <%-- Create actions menu --%>
                                 <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren" id="eval3">
                                       <a:actionLink value="#{msg.create_content}" image="/images/icons/new_content.gif" id="link3_1" action="createContent" actionListener="#{CreateContentWizard.startWizard}" />
                                    </r:permissionEvaluator>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren" id="eval1">
                                       <a:actionLink value="#{msg.new_space}" image="/images/icons/create_space.gif" action="createSpace" actionListener="#{NewSpaceDialog.startWizard}" id="link1" />
                                    </r:permissionEvaluator>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren" id="eval6">
                                       <a:actionLink value="#{msg.advanced_space_wizard}" image="/images/icons/create_space.gif" action="createAdvancedSpace" actionListener="#{NewSpaceWizard.startWizard}" id="link9" />
                                    </r:permissionEvaluator>
                                 </a:menu>
                              </td>
                              <td style="padding-left:4px" width=80>
                                 <%-- More actions menu --%>
                                 <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <a:actionLink value="#{msg.view_details}" image="/images/icons/View_details.gif" action="dialog:showSpaceDetails" actionListener="#{BrowseBean.setupSpaceAction}" id="link5">
                                       <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param6" />
                                    </a:actionLink>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="Delete" id="eval4">
                                       <a:actionLink value="#{msg.delete_space}" image="/images/icons/delete.gif" action="dialog:deleteSpace" actionListener="#{BrowseBean.setupDeleteAction}" id="link2">
                                          <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param1" />
                                       </a:actionLink>
                                       <a:actionLink value="#{msg.cut}" image="/images/icons/cut.gif" id="link6" actionListener="#{ClipboardBean.cutNode}">
                                          <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param3" />
                                       </a:actionLink>
                                    </r:permissionEvaluator>
                                    <a:actionLink value="#{msg.copy}" image="/images/icons/copy.gif" id="link7" actionListener="#{ClipboardBean.copyNode}">
                                       <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param4" />
                                    </a:actionLink>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="Write" id="eval5">
                                       <a:actionLink value="#{msg.paste_all}" image="/images/icons/paste.gif" actionListener="#{ClipboardBean.pasteAll}" id="link8" />
                                    </r:permissionEvaluator>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="ChangePermissions" id="eval3_1">
                                       <a:actionLink value="#{msg.manage_invited_users}" image="/images/icons/invite.gif" id="link4" action="dialog:manageInvitedUsers" actionListener="#{BrowseBean.setupSpaceAction}">
                                          <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param6_1" />
                                       </a:actionLink>
                                    </r:permissionEvaluator>
                                    <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="Write" id="eval7">
                                       <a:actionLink value="#{msg.manage_rules}" image="/images/icons/rule.gif" action="dialog:manageRules" actionListener="#{BrowseBean.setupSpaceAction}" id="link10">
                                          <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param5" />
                                       </a:actionLink>
                                    </r:permissionEvaluator>
                                    <%-- admin user only actions --%>
                                    <a:booleanEvaluator value="#{NavigationBean.currentUser.admin == true}" id="eval8">
                                       <a:actionLink value="#{msg.admin_console}" image="/images/icons/admin_console.gif" action="adminConsole" id="link11" />
                                    </a:booleanEvaluator>
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
                                       <a:actionLink value="#{msg.save_new_search}" image="/images/icons/save_search.gif" padding="4" action="#{AdvancedSearchBean.saveNewSearch}" id="link20_1" />
                                       <a:booleanEvaluator value="#{AdvancedSearchBean.allowEdit == true}" id="eval0_1">
                                          <a:actionLink value="#{msg.save_edit_search}" image="/images/icons/edit_search.gif" padding="4" action="#{AdvancedSearchBean.saveEditSearch}" id="link20_2" />
                                       </a:booleanEvaluator>
                                    </a:booleanEvaluator>
                                 </a:menu>
                              </td>
                           </a:panel>
                           
                           <td class="separator" width=1></td>
                           <td width=118 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="4" iconColumnWidth="20" selectedStyleClass="statusListHighlight" disabledStyleClass="statusListDisabled" selectedImage="/images/icons/Details.gif"
                                    value="#{BrowseBean.browseViewMode}" actionListener="#{BrowseBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="details" label="#{msg.details_view}" />
                                 <a:listItem value="icons" label="#{msg.view_icon}" />
                                 <a:listItem value="list" label="#{msg.view_browse}" />
                                 <a:listItem value="dashboard" label="#{msg.dashboard_view}" disabled="#{!NavigationBean.currentNodeHasTemplate}" />
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
               
               <%-- Details - Spaces --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <a:panel id="spaces-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{msg.browse_spaces}">
                     
                     <%-- Spaces List --%>
                     <a:richList id="spacesList" binding="#{BrowseBean.spacesRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.browsePageSize}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.nodes}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <%-- TODO: either build complete message in BrowseBean or have no icon... --%>
                           <h:outputFormat value="#{msg.no_space_items}" escape="false" rendered="#{NavigationBean.searchContext == null}">
                              <f:param value="#{msg.new_space}" />
                           </h:outputFormat>
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column primary="true" width="200" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Primary column for icons view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="header">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Primary column for list view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="title">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Description column for all view modes --%>
                        <a:column style="text-align:left">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.description}" />
                        </a:column>
                        
                        <%-- Path column for search mode in details view mode --%>
                        <a:column style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.path}" value="displayPath" styleClass="header"/>
                           </f:facet>
                           <r:nodePath value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                        </a:column>
                        
                        <%-- Created Date column for details view mode --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.created}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Modified Date column for details/icons view modes --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.modified}" value="modified" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.modified}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Node Descendants links for list view mode --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <r:nodeDescendants value="#{r.nodeRef}" styleClass="header" actionListener="#{BrowseBean.clickDescendantSpace}" />
                        </a:column>
                        
                        <%-- Space Actions column --%>
                        <a:column actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.actions}"/>
                           </f:facet>
                           <a:actionLink value="#{msg.preview}" image="/images/icons/preview.gif" showLink="false" styleClass="inlineAction" actionListener="#{BrowseBean.setupSpaceAction}" action="previewSpace">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                           <a:booleanEvaluator value="#{r.beingDiscussed == true}">
                              <a:actionLink value="#{msg.discuss}" image="/images/icons/forum.gif" showLink="false" styleClass="inlineAction" actionListener="#{ForumsBean.discuss}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </a:booleanEvaluator>
                           <r:permissionEvaluator value="#{r}" allow="Delete">
                              <a:actionLink value="#{msg.cut}" image="/images/icons/cut.gif" showLink="false" styleClass="inlineAction" actionListener="#{ClipboardBean.cutNode}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </r:permissionEvaluator>
                           <a:actionLink value="#{msg.copy}" image="/images/icons/copy.gif" showLink="false" styleClass="inlineAction" actionListener="#{ClipboardBean.copyNode}">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                           <a:actionLink value="#{msg.view_details}" image="/images/icons/View_details.gif" showLink="false" styleClass="inlineAction" action="dialog:showSpaceDetails" actionListener="#{BrowseBean.setupSpaceAction}">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                           <%-- More actions menu --%>
                           <a:menu itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                              <r:permissionEvaluator value="#{r}" allow="Delete">
	                              <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif" action="dialog:deleteSpace" actionListener="#{BrowseBean.setupDeleteAction}">
	                                 <f:param name="id" value="#{r.id}" />
	                              </a:actionLink>
                           	</r:permissionEvaluator>
                           	<r:permissionEvaluator value="#{r}" allow="CreateChildren">
	                              <a:booleanEvaluator value="#{r.beingDiscussed == false}">
	                                 <a:actionLink value="#{msg.start_discussion}" image="/images/icons/create_forum.gif" actionListener="#{CreateDiscussionDialog.startWizard}">
	                                    <f:param name="id" value="#{r.id}" />
	                                 </a:actionLink>
	                              </a:booleanEvaluator>
                              </r:permissionEvaluator>
                           </a:menu>
                        </a:column>
                        
                        <a:dataPager styleClass="pager" />
                     </a:richList>
                     
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Content --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <a:panel id="content-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{msg.browse_content}">
                     
                     <%-- Content list --%>
                     <a:richList id="contentRichList" binding="#{BrowseBean.contentRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.browsePageSize}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{BrowseBean.content}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <%-- TODO: either build complete message in BrowseBean or have no icon... --%>
                           <h:outputFormat value="#{msg.no_content_items}" escape="false" rendered="#{NavigationBean.searchContext == null}">
                              <f:param value="#{msg.add_content}" />
                              <f:param value="#{msg.create_content}" />
                           </h:outputFormat>
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column primary="true" width="200" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink value="#{r.name}" href="#{r.url}" target="new" />
                           <r:lockIcon value="#{r.nodeRef}" align="absmiddle" />
                        </a:column>
                        
                        <%-- Primary column for icons view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink value="#{r.name}" href="#{r.url}" target="new" styleClass="header" />
                           <r:lockIcon value="#{r.nodeRef}" align="absmiddle" />
                        </a:column>
                        
                        <%-- Primary column for list view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType32}" showLink="false" styleClass="inlineAction" />
                           </f:facet>
                           <a:actionLink value="#{r.name}" href="#{r.url}" target="new" styleClass="title" />
                           <r:lockIcon value="#{r.nodeRef}" align="absmiddle" />
                        </a:column>
                        
                        <%-- Description column for all view modes --%>
                        <a:column style="text-align:left">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.description}" />
                        </a:column>
                        
                        <%-- Path column for search mode in details view mode --%>
                        <a:column style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.path}" value="displayPath" styleClass="header"/>
                           </f:facet>
                           <r:nodePath value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                        </a:column>
                        
                        <%-- Size for details/icons view modes --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.size}" value="size" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.size}">
                              <a:convertSize />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Created Date column for details view mode --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.created}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Modified Date column for details/icons view modes --%>
                        <a:column style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.modified}" value="modified" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.modified}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Content Actions column --%>
                        <a:column actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.actions}"/>
                           </f:facet>
                           <r:permissionEvaluator value="#{r}" allow="Write">
                              <a:booleanEvaluator value="#{(r.locked == false && r.workingCopy == false) || r.owner == true}">
                                 <a:booleanEvaluator value="#{r.editLinkType == 'http'}">
                                    <a:actionLink value="#{msg.edit}" image="/images/icons/edit_icon.gif" showLink="false" styleClass="inlineAction" actionListener="#{CheckinCheckoutBean.editFile}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value="#{r.editLinkType == 'webdav'}">
                                    <a:actionLink value="#{msg.edit}" image="/images/icons/edit_icon.gif" showLink="false" styleClass="inlineAction" onclick="javascript:openDoc('#{r.webdavUrl}');" />
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value="#{r.editLinkType == 'cifs'}">
                                    <a:actionLink value="#{msg.edit}" image="/images/icons/edit_icon.gif" showLink="false" styleClass="inlineAction" href="#{r.cifsPath}" target="new" />
                                 </a:booleanEvaluator>
                              </a:booleanEvaluator>
                           </r:permissionEvaluator>
                           <r:permissionEvaluator value="#{r}" allow="CheckOut">
                              <a:booleanEvaluator value="#{r.locked == false && r.workingCopy == false}">
                                 <a:actionLink value="#{msg.checkout}" image="/images/icons/CheckOut_icon.gif" showLink="false" styleClass="inlineAction" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkoutFile">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </a:booleanEvaluator>
                           </r:permissionEvaluator>   
                           <a:booleanEvaluator value="#{r.checkIn == true}">
                              <a:actionLink value="#{msg.checkin}" image="/images/icons/CheckIn_icon.gif" showLink="false" styleClass="inlineAction" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkinFile">
                                 <f:param name="id" value="#{r.id}" />
                             </a:actionLink>
                           </a:booleanEvaluator>
                           <a:booleanEvaluator value="#{r.beingDiscussed == true}">
                              <a:actionLink value="#{msg.discuss}" image="/images/icons/forum.gif" showLink="false" styleClass="inlineAction" actionListener="#{ForumsBean.discuss}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </a:booleanEvaluator>
                           <a:actionLink value="#{msg.view_details}" image="/images/icons/View_details.gif" showLink="false" styleClass="inlineAction" actionListener="#{BrowseBean.setupContentAction}" action="dialog:showDocDetails">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                           <a:actionLink value="#{msg.preview}" image="/images/icons/preview.gif" showLink="false" styleClass="inlineAction" actionListener="#{BrowseBean.setupContentAction}" action="previewContent">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                           <%-- More actions menu --%>
                           <a:menu itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
                              <r:permissionEvaluator value="#{r}" allow="Delete">
	                              <a:booleanEvaluator value="#{r.locked == false && r.workingCopy == false}">
	                                 <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif" actionListener="#{BrowseBean.setupContentAction}" action="dialog:deleteFile">
	                                    <f:param name="id" value="#{r.id}" />
	                                 </a:actionLink>
	                              </a:booleanEvaluator>
                              </r:permissionEvaluator>
                              <r:permissionEvaluator value="#{r}" allow="Write">
                                 <a:booleanEvaluator value="#{(r.locked == false && r.workingCopy == false) || r.owner == true}">
                                    <a:actionLink value="#{msg.update}" image="/images/icons/update.gif" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="updateFile">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </r:permissionEvaluator>
                              <a:booleanEvaluator value="#{r.cancelCheckOut == true}">
                                 <a:actionLink value="#{msg.undocheckout}" image="/images/icons/undo_checkout.gif" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="undoCheckoutFile">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </a:booleanEvaluator>
                              <a:booleanEvaluator value='#{r["app:approveStep"] != null && r.workingCopy == false && r.locked == false}'>
                                 <a:actionLink value='#{r["app:approveStep"]}' image="/images/icons/approve.gif" actionListener="#{DocumentDetailsBean.approve}">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </a:booleanEvaluator>
                              <a:booleanEvaluator value='#{r["app:rejectStep"] != null && r.workingCopy == false && r.locked == false}'>
                                 <a:actionLink value='#{r["app:rejectStep"]}' image="/images/icons/reject.gif" actionListener="#{DocumentDetailsBean.reject}">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </a:booleanEvaluator>
                              <r:permissionEvaluator value="#{r}" allow="Delete">
                                 <a:actionLink value="#{msg.cut}" image="/images/icons/cut.gif" actionListener="#{ClipboardBean.cutNode}">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </r:permissionEvaluator>
                              <a:actionLink value="#{msg.copy}" image="/images/icons/copy.gif" actionListener="#{ClipboardBean.copyNode}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                              <r:permissionEvaluator value="#{r}" allow="CreateChildren">
	                              <a:booleanEvaluator value="#{r.beingDiscussed == false && r.locked == false}">
	                                 <a:actionLink value="#{msg.start_discussion}" image="/images/icons/create_forum.gif" actionListener="#{CreateDiscussionDialog.startWizard}">
	                                    <f:param name="id" value="#{r.id}" />
	                                 </a:actionLink>
	                              </a:booleanEvaluator>
	                           </r:permissionEvaluator>
                           </a:menu>
                        </a:column>
                        
                        <a:dataPager styleClass="pager" />
                     </a:richList>
                     
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
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

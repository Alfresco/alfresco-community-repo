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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_file_details">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="document-details">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2">
      
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
         <td width="100%">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <img src="<%=request.getContextPath()%>/images/icons/details_large.gif" width=32 height=32>
                           </td>
                           <td>
                              <div class="mainTitle">
                                 <h:outputText value="#{msg.details_of}" /> '<h:outputText value="#{DocumentDetailsBean.name}" />'<r:lockIcon value="#{DocumentDetailsBean.document.nodeRef}" align="absmiddle" />
                              </div>
                              <div class="mainSubText"><h:outputText value="#{msg.location}" />: <r:nodePath value="#{DocumentDetailsBean.document.nodeRef}" breadcrumb="true" actionListener="#{BrowseBean.clickSpacePath}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.documentdetails_description}" /></div>
                           </td>
                           
                           <td align=right>
                              <%-- Checkin/Checkout action --%>
                              <nobr>
                              <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="CheckOut">
                                 <a:booleanEvaluator value="#{DocumentDetailsBean.locked == false && DocumentDetailsBean.workingCopy == false}">
                                    <a:actionLink value="#{msg.checkout}" image="/images/icons/CheckOut_icon.gif" padding="4" style="white-space:nowrap" 
                                                  actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkoutFile">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </r:permissionEvaluator>
                              <a:booleanEvaluator value="#{DocumentDetailsBean.document.properties.checkIn == true}">
                                 <a:actionLink value="#{msg.checkin}" image="/images/icons/CheckIn_icon.gif" padding="4" style="white-space:nowrap" 
                                               actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkinFile">
                                    <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                 </a:actionLink>
                              </a:booleanEvaluator>
                              </nobr>
                           </td>
                           
                           <td style="padding-left:4px" width=64>
                              <%-- Actions menu --%>
                              <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.actions}" image="/images/icons/menu.gif"
                                    menuStyleClass="moreActionsMenu" style="white-space:nowrap" tooltip="#{msg.more_options_file}">
                                 
                                 <a:booleanEvaluator value="#{DocumentDetailsBean.document.properties.cancelCheckOut == true}">
                                    <a:actionLink value="#{msg.undocheckout}" image="/images/icons/undo_checkout.gif" 
                                                  actionListener="#{CheckinCheckoutBean.setupContentAction}" action="undoCheckoutFile">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                                 
                                 <%-- approve and reject --%>
                                 <a:booleanEvaluator value="#{DocumentDetailsBean.approveStepName != null && DocumentDetailsBean.workingCopy == false && DocumentDetailsBean.locked == false}">
                                    <a:actionLink value="#{DocumentDetailsBean.approveStepName}" image="/images/icons/approve.gif"
                                                  actionListener="#{DocumentDetailsBean.approve}" action="browse">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value="#{DocumentDetailsBean.rejectStepName != null && DocumentDetailsBean.workingCopy == false && DocumentDetailsBean.locked == false}">
                                    <a:actionLink value="#{DocumentDetailsBean.rejectStepName}" image="/images/icons/reject.gif"
                                                  actionListener="#{DocumentDetailsBean.reject}" action="browse">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              
                                 <%-- edit and update --%>
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write">
                                    <a:booleanEvaluator value="#{(DocumentDetailsBean.locked == false && DocumentDetailsBean.workingCopy == false) || DocumentDetailsBean.owner == true}">
                                       <a:actionLink value="#{msg.edit}" image="/images/icons/edit_icon.gif"
                                                     actionListener="#{CheckinCheckoutBean.editFile}">
                                          <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                       </a:actionLink>
                                       <a:actionLink value="#{msg.update}" image="/images/icons/update.gif"
                                                     actionListener="#{CheckinCheckoutBean.setupContentAction}" action="updateFile">
                                          <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                       </a:actionLink>
                                    </a:booleanEvaluator>
                                 </r:permissionEvaluator>
                                 
                                 <%-- cut --%>
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Delete">
                                    <a:actionLink value="#{msg.cut}" image="/images/icons/cut.gif"
                                                  actionListener="#{ClipboardBean.cutNode}">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </r:permissionEvaluator>
                                 
                                 <%-- copy --%>
                                 <a:actionLink value="#{msg.copy}" image="/images/icons/copy.gif"
                                               actionListener="#{ClipboardBean.copyNode}">
                                    <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                 </a:actionLink>
                                 
                                 <%-- delete --%>
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Delete">
                                    <a:booleanEvaluator value="#{DocumentDetailsBean.locked == false && DocumentDetailsBean.workingCopy == false}">
                                       <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif"
                                                     actionListener="#{BrowseBean.setupContentAction}" action="dialog:deleteFile">
                                          <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                       </a:actionLink>
                                    </a:booleanEvaluator>
                                 </r:permissionEvaluator>
                                 
                                 <%-- Take Ownership --%>
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="TakeOwnership">
                                    <a:actionLink value="#{msg.take_ownership}" image="/images/icons/take_ownership.gif" actionListener="#{DocumentDetailsBean.takeOwnership}" id="takeOwnership" />
                                 </r:permissionEvaluator>
                                 
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="ChangePermissions">
                                    <a:actionLink value="#{msg.manage_content_users}" image="/images/icons/invite.gif" action="dialog:manageContentUsers" actionListener="#{BrowseBean.setupContentAction}">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </r:permissionEvaluator>
                              
                                 <%-- create shortcut --%>
                                 <a:actionLink value="#{msg.create_shortcut}" image="/images/icons/shortcut.gif" actionListener="#{UserShortcutsBean.createShortcut}" rendered="#{NavigationBean.isGuest == false}">
                                    <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                 </a:actionLink>
                                 
                                 <%-- discussion --%>
                                 <a:booleanEvaluator value='#{DocumentDetailsBean.document.properties["beingDiscussed"] == true}'>
                                    <a:actionLink value="#{msg.discuss}" image="/images/icons/forum.gif" actionListener="#{ForumsBean.discuss}">
                                       <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value='#{DocumentDetailsBean.document.properties["beingDiscussed"] == false}'>
                                    <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="CreateChildren">
   	                                 <a:actionLink value="#{msg.start_discussion}" image="/images/icons/create_forum.gif" actionListener="#{CreateDiscussionDialog.startWizard}">
   	                                    <f:param name="id" value="#{DocumentDetailsBean.id}" />
   	                                 </a:actionLink>
                                    </r:permissionEvaluator>
                                 </a:booleanEvaluator>
                                 
                                 <%-- preview in template --%>
                                 <a:actionLink value="#{msg.preview}" image="/images/icons/preview.gif" actionListener="#{BrowseBean.setupContentAction}" action="previewContent">
                                    <f:param name="id" value="#{DocumentDetailsBean.id}" />
                                 </a:actionLink>
                                 
                                 <%-- custom action --%>
                                 <a:actionLink value="#{msg.other_action}" image="/images/icons/action.gif" action="createAction" actionListener="#{NewActionWizard.startWizard}" />
                              </a:menu>
                           </td>
                           
                           
                           <%-- TODO: FINISH --%>
                           
                           
                           <%-- Navigation --%>
                           <td class="separator" width=1></td>
                           <td width=100>
                              <h:outputText style="padding-left:20px" styleClass="mainSubTitle" value="#{msg.navigation}" /><br>
                              <a:actionLink value="#{msg.next_item}" image="/images/icons/NextItem.gif" padding="4" actionListener="#{DocumentDetailsBean.nextItem}" action="nextItem">
                                 <f:param name="id" value="#{DocumentDetailsBean.id}" />
                              </a:actionLink>
                              <a:actionLink value="#{msg.previous_item}" image="/images/icons/PreviousItem.gif" padding="4" actionListener="#{DocumentDetailsBean.previousItem}" action="previousItem">
                                 <f:param name="id" value="#{DocumentDetailsBean.id}" />
                              </a:actionLink>
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
               
               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              <a:panel label="#{msg.view_links}" id="preview-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE"
                                       expanded='#{DocumentDetailsBean.panels["preview-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
                                    <tr>
                                       <td>
                                          <a:actionLink value="#{msg.view_in_browser}" href="#{DocumentDetailsBean.browserUrl}" target="new" id="link1" />
                                       </td>
                                       <td>
                                          <a:actionLink value="#{msg.view_in_webdav}" href="#{DocumentDetailsBean.webdavUrl}" target="new" id="link2" />
                                       </td>
                                       <td>
                                          <a:actionLink value="#{msg.view_in_cifs}" href="#{DocumentDetailsBean.cifsPath}" target="new" id="link3" />
                                       </td>
                                    </tr>
                                    <tr>
                                       <td>
                                          <a:actionLink value="#{msg.download_content}" href="#{DocumentDetailsBean.downloadUrl}" target="new" id="link4" />
                                       </td>
                                       <td>
                                          <a href='<%=request.getContextPath()%><a:outputText value="#{DocumentDetailsBean.bookmarkUrl}" id="out1" />' onclick="return false;"><a:outputText value="#{msg.details_page_bookmark}" id="out2" /></a>
                                       </td>
                                       <td>
                                          <a href='<a:outputText value="#{DocumentDetailsBean.nodeRefUrl}" id="out3" />' onclick="return false;"><a:outputText value="#{msg.noderef_link}" id="out4" /></a>
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                              <br/>
                              
                              <h:panelGroup id="props-panel-facets">
                                 <f:facet name="title">
                                    <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write">
                                       <a:actionLink id="titleLink1" value="#{msg.modify}" showLink="false" image="/images/icons/Change_details.gif"
                                             action="editDocProperties" actionListener="#{EditDocPropsDialog.setupDocumentForAction}" />
                                    </r:permissionEvaluator>
                                 </f:facet>
                              </h:panelGroup>
                              <a:panel label="#{msg.properties}" id="properties-panel" facetsId="props-panel-facets" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE"
                                       rendered="#{DocumentDetailsBean.locked == false}"
                                       expanded='#{DocumentDetailsBean.panels["properties-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <table cellspacing="0" cellpadding="0" border="0" width="100%">
                                    <tr>
                                       <td width=80 align=center>
                                          <%-- icon image for the doc --%>
                                          <table cellspacing=0 cellpadding=0 border=0>
                                             <tr>
                                                <td>
                                                   <div style="border: thin solid #CCCCCC; padding:4px">
                                                      <a:actionLink id="doc-logo1" value="#{DocumentDetailsBean.name}" href="#{DocumentDetailsBean.url}" target="new" image="#{DocumentDetailsBean.document.properties.fileType32}" showLink="false" />
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
                                          <r:propertySheetGrid id="document-props" value="#{DocumentDetailsBean.document}" var="documentProps" 
                                                      columns="1" mode="view" labelStyleClass="propertiesLabel" 
                                                      externalConfig="true" />
                                          <h:outputText id="no-inline-msg" value="<br/>#{msg.not_inline_editable}<br/><br/>"
                                               rendered="#{DocumentDetailsBean.inlineEditable == false}" escape="false" />
                                          <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write" id="eval_inline">
                                             <a:actionLink id="make-inline" value="#{msg.allow_inline_editing}" 
                                                  action="#{DocumentDetailsBean.applyInlineEditable}"
                                                  rendered="#{DocumentDetailsBean.inlineEditable == false}" />
                                          </r:permissionEvaluator>
                                          <h:messages globalOnly="true" id="props-msgs" styleClass="errorMessage" layout="table" />
                                          <h:message for="takeOwnership" styleClass="statusMessage" />
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                              <br/>
                              
                              <a:panel label="#{msg.properties}" id="properties-panel-locked" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" rendered="#{DocumentDetailsBean.locked}"
                                       expanded='#{DocumentDetailsBean.panels["properties-panel-locked"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <table cellspacing="0" cellpadding="0" border="0" width="100%">
                                    <tr>
                                       <td width=80 align=center>
                                          <%-- icon image for the doc --%>
                                          <table cellspacing=0 cellpadding=0 border=0>
                                             <tr>
                                                <td>
                                                   <div style="border: thin solid #CCCCCC; padding:4px">
                                                      <a:actionLink id="doc-logo2" value="#{DocumentDetailsBean.name}" href="#{DocumentDetailsBean.url}" target="new" image="#{DocumentDetailsBean.document.properties.fileType32}" showLink="false" />
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
                                          <r:propertySheetGrid id="document-props-locked" value="#{DocumentDetailsBean.document}" var="documentProps" 
                                                      columns="1" mode="view" labelStyleClass="propertiesLabel" 
                                                      externalConfig="true" />
                                          <h:outputText id="no-inline-msg2" value="<br/>#{msg.not_inline_editable}<br/>"
                                               rendered="#{DocumentDetailsBean.inlineEditable == false}" escape="false" />
                                          <h:messages id="props-locked-msgs" styleClass="errorMessage" layout="table" />
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                              <h:panelGroup id="workflow-panel-facets">
                                 <f:facet name="title">
                                    <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write">
                                       <a:actionLink id="titleLink2" value="#{msg.workflow}" showLink="false" image="/images/icons/Change_details.gif" action="editSimpleWorkflow" />
                                    </r:permissionEvaluator>
                                 </f:facet>
                              </h:panelGroup>
                              <a:panel label="#{msg.workflow}" id="workflow-panel" facetsId="workflow-panel-facets" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" rendered="#{DocumentDetailsBean.approveStepName != null}"
                                       expanded='#{DocumentDetailsBean.panels["workflow-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <h:outputText id="workflow-overview" value="#{DocumentDetailsBean.workflowOverviewHTML}" 
                                               escape="false" />
                              </a:panel>
                              <a:panel label="#{msg.workflow}" id="no-workflow-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" rendered="#{DocumentDetailsBean.approveStepName == null}"
                                       expanded='#{DocumentDetailsBean.panels["workflow-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <h:outputText id="no-workflow-msg" value="#{msg.not_in_workflow}" />
                              </a:panel>
                              
                              <br/>
                              
                              <h:panelGroup id="category-panel-facets">
                                 <f:facet name="title">
                                    <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write">
                                       <a:actionLink id="titleLink3" value="#{msg.change_category}" showLink="false" image="/images/icons/Change_details.gif"
                                             action="editCategories" actionListener="#{DocumentDetailsBean.setupCategoriesForEdit}" />
                                    </r:permissionEvaluator>
                                 </f:facet>
                              </h:panelGroup>
                              <a:panel label="#{msg.category}" id="category-panel" facetsId="category-panel-facets" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" rendered="#{DocumentDetailsBean.categorised}"
                                       expanded='#{DocumentDetailsBean.panels["category-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <h:outputText id="category-overview" value="#{DocumentDetailsBean.categoriesOverviewHTML}" 
                                               escape="false" />
                              </a:panel>
                              <a:panel label="#{msg.category}" id="no-category-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE"
                                       rendered="#{DocumentDetailsBean.categorised == false}"
                                       expanded='#{DocumentDetailsBean.panels["category-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <h:outputText id="no-category-msg" value="#{msg.not_in_category}<br/><br/>" 
                                               escape="false"/>
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write" id="eval_cat">
                                    <a:actionLink id="make-classifiable" value="#{msg.allow_categorization}" 
                                                  action="#{DocumentDetailsBean.applyClassifiable}"
                                                  rendered="#{DocumentDetailsBean.locked == false}" />
                                 </r:permissionEvaluator>
                              </a:panel>
                              
                              <br/>
                              
                              <a:panel label="#{msg.version_history}" id="version-history-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" rendered="#{DocumentDetailsBean.versionable}"
                                       expanded='#{DocumentDetailsBean.panels["version-history-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 
                                 <a:richList id="versionHistoryList" viewMode="details" value="#{DocumentDetailsBean.versionHistory}" 
                                             var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" 
                                             rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" 
                                             pageSize="10" initialSortColumn="versionLabel" initialSortDescending="false">
                                    
                                    <%-- Primary column for details view mode --%>
                                    <a:column id="col1" primary="true" width="100" style="padding:2px;text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink label="#{msg.version}" value="versionLabel" mode="case-insensitive" styleClass="header"/>
                                       </f:facet>
                                       <a:actionLink id="label" value="#{r.versionLabel}" href="#{r.url}" target="new" />
                                    </a:column>
                                    
                                    <%-- Version notes columns --%>
                                    <a:column id="col2" width="200" style="text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink label="#{msg.notes}" value="notes" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="notes" value="#{r.notes}" />
                                    </a:column>
                                    
                                    <%-- Description columns --%>
                                    <a:column id="col3" style="text-align:left">
                                       <f:facet name="header">
                                          <a:sortLink label="#{msg.author}" value="author" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="author" value="#{r.author}" />
                                    </a:column>
                                    
                                    <%-- Created Date column for details view mode --%>
                                    <a:column id="col4" style="text-align:left; white-space:nowrap">
                                       <f:facet name="header">
                                          <a:sortLink label="#{msg.date}" value="versionDate" styleClass="header"/>
                                       </f:facet>
                                       <h:outputText id="date" value="#{r.versionDate}">
                                          <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                                       </h:outputText>
                                    </a:column>
                                    
                                    <%-- view the contents of the specific version --%>
                                    <a:column id="col5" style="text-align: left">
                                       <f:facet name="header">
                                          <h:outputText value="#{msg.actions}"/>
                                       </f:facet>
                                       <a:actionLink id="view-link" value="View" href="#{r.url}" target="new" />
                                    </a:column>
              
                                    <a:dataPager styleClass="pager" />
                                 </a:richList>
                              </a:panel>
                              <a:panel label="#{msg.version_history}" id="no-version-history-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE"
                                       rendered="#{DocumentDetailsBean.versionable == false}"
                                       expanded='#{DocumentDetailsBean.panels["version-history-panel"]}' expandedActionListener="#{DocumentDetailsBean.expandPanel}">
                                 <h:outputText id="no-history-msg" value="#{msg.not_versioned}<br/><br/>" 
                                               escape="false" />
                                 <r:permissionEvaluator value="#{DocumentDetailsBean.document}" allow="Write" id="eval_ver">
                                    <a:actionLink id="make-versionable" value="#{msg.allow_versioning}"
                                                  action="#{DocumentDetailsBean.applyVersionable}" 
                                                  rendered="#{DocumentDetailsBean.locked == false}" />
                                 </r:permissionEvaluator>
                              </a:panel>
                              <br/>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="dialog:close" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
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
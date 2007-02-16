<%--
  Copyright (C) 2005 Alfresco, Inc.
 
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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<r:page titleId="title_forums">

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="browse-forums">
   
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
 
                           <%-- actions for forums --%>
                           <a:panel id="forums-actions">
                              <td width=32>
                                 <h:graphicImage id="space-logo" url="/images/icons/forums.gif" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" /></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.forums_info}" id="msg3" /></div>
                                 <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                              </td>
                              
                              <td style="padding-left:4px" width=52>
                                 <%-- Create actions menu --%>
                                 <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="actions_create" value="forums_create_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                              
                              <td style="padding-left:4px" width=80>
                                 <%-- More actions menu --%>
                                 <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="actions_more" value="forums_actions_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                           </a:panel>
                          
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                           <td width=110 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{ForumsBean.forumsViewMode}" actionListener="#{ForumsBean.forumsViewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
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
               
               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%> 
                     <a:errors message="" infoClass="statusWarningText" errorClass="statusErrorText" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Forums --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     <a:panel id="forums-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.browse_forums}">
                     
                     <%-- Forums List --%>
                     <a:richList id="forumsList" binding="#{ForumsBean.forumsRichList}" viewMode="#{ForumsBean.forumsViewMode}" pageSize="#{ForumsBean.forumsPageSize}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{ForumsBean.forums}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <h:outputFormat value="#{msg.no_forums}" escape="false" />
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column primary="true" width="200" style="padding:2px;text-align:left" rendered="#{ForumsBean.forumsViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" action="showTopics" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Primary column for icons view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{ForumsBean.forumsViewMode == 'icons'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" action="showTopics" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="header">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Primary column for list view mode --%>
                        <a:column primary="true" style="padding:2px;text-align:left" rendered="#{ForumsBean.forumsViewMode == 'list'}">
                           <f:facet name="large-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" action="showTopics" showLink="false">
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
                        <a:column style="text-align:left" rendered="#{NavigationBean.searchContext != null && ForumsBean.forumsViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.path}" value="displayPath" styleClass="header"/>
                           </f:facet>
                           <r:nodePath value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
                        </a:column>
                        
                        <%-- Created Date column for details view mode --%>
                        <a:column style="text-align:left" rendered="#{ForumsBean.forumsViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.created}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Modified Date column for details/icons view modes --%>
                        <a:column style="text-align:left" rendered="#{ForumsBean.forumsViewMode == 'details' || ForumsBean.forumsViewMode == 'icons'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.modified}" value="modified" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.modified}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Node Descendants links for list view mode --%>
                        <a:column style="text-align:left" rendered="#{ForumsBean.forumsViewMode == 'list'}">
                           <r:nodeDescendants value="#{r.nodeRef}" styleClass="header" actionListener="#{BrowseBean.clickDescendantSpace}" />
                        </a:column>
                        
                        <%-- Actions column --%>
                        <a:column id="col1" actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.actions}"/>
                           </f:facet>
                           
                           <%-- actions are configured in web-client-config-forum-actions.xml --%>
                           <r:actions id="actions" value="forums_actions" context="#{r}" showLink="false" styleClass="inlineAction" />
                        </a:column>
                        
                        <a:dataPager styleClass="pager" />
                     </a:richList>
                     
                     </a:panel>
                     
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

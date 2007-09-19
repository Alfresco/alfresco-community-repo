<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
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
--%><%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_groups_list">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="groups">
   
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
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="logo" url="/images/icons/group_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.groups_management}" /></div>
                              <div class="mainSubTitle">
                                 <%-- show either root message or the current group name --%>
                                 <h:outputText value="#{msg.root_groups}" rendered="#{GroupsProperties.group == null}" />
                                 <h:outputText value="#{GroupsProperties.groupName}" rendered="#{GroupsProperties.group != null}" />
                              </div>
                              <div class="mainSubText"><h:outputText value="#{msg.groups_description}" /></div>
                           </td>
                           
                           <td align=right>
                              <%-- Create actions menu --%>
                              <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:booleanEvaluator value="#{GroupsProperties.group == null}">
                                    <a:actionLink value="#{msg.new_group}" image="/images/icons/create_group.gif" action="dialog:createGroup" actionListener="#{GroupsDialog.clearGroupAction}" />
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value="#{GroupsProperties.group != null}">
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" action="dialog:createGroup" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsProperties.group}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </a:menu>
                           </td>
                           <td style="padding-left:4px" width=80>
                              <%-- More actions menu --%>
                              <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:booleanEvaluator value="#{GroupsProperties.group != null}">
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" action="dialog:createGroup" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsProperties.group}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete_group}" image="/images/icons/delete_group.gif" action="dialog:deleteGroup" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsProperties.group}" />
                                    </a:actionLink>
                                    <%-- TODO: should this be add user(S) - multiple required on generic picker? --%>
                                    <a:actionLink value="#{msg.add_user}" image="/images/icons/add_user.gif" action="dialog:addUsers" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsProperties.group}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </a:menu>
                           </td>
                          <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                          <td style="padding-left:4px" width=80 valign=middle>
                              <%-- Filter settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/filter.gif"
                                    value="#{GroupsProperties.filterMode}" actionListener="#{GroupsDialog.filterModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="children" label="#{msg.group_filter_children}" />
                                 <a:listItem value="all" label="#{msg.group_filter_all}" />
                              </a:modeList>
                           </td>
                          <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                          <td style="padding-left:4px" width=80 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{GroupsProperties.viewMode}" actionListener="#{GroupsDialog.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="icons" label="#{msg.group_icons}" />
                                 <a:listItem value="details" label="#{msg.group_details}" />
                              </a:modeList>
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
                              
                              <%-- Group Path Breadcrumb --%>
                              <div style="padding-left:8px;padding-top:4px;padding-bottom:4px">
                                 <a:breadcrumb value="#{GroupsDialog.location}" styleClass="title" />
                              </div>
                              
                              <%-- Groups List --%>
                              <div style="padding:4px">
                              
                              <a:panel id="groups-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.groups}">
                              
                              <a:richList id="groups-list" binding="#{GroupsProperties.groupsRichList}" viewMode="#{GroupsProperties.viewMode}" pageSize="12"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{GroupsDialog.groups}" var="r" initialSortColumn="name" initialSortDescending="true">
                                 
                                 <%-- Primary column for icons view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top" rendered="#{GroupsProperties.viewMode == 'icons'}">
                                    <f:facet name="large-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/group_large.gif" actionListener="#{GroupsDialog.clickGroup}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" actionListener="#{GroupsDialog.clickGroup}" styleClass="header">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Primary column for details view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left" rendered="#{GroupsProperties.viewMode == 'details'}">
                                    <f:facet name="small-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/group.gif" actionListener="#{GroupsDialog.clickGroup}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.identifier}" value="name" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" actionListener="#{GroupsDialog.clickGroup}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" showLink="false" styleClass="inlineAction" action="dialog:createGroup" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.add_user}" image="/images/icons/add_user.gif" showLink="false" styleClass="inlineAction" action="dialog:addUsers" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete}" image="/images/icons/delete_group.gif" showLink="false" styleClass="inlineAction" action="dialog:deleteGroup" actionListener="#{GroupsDialog.setupGroupAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <a:dataPager/>
                              </a:richList>
                              
                              </a:panel>
                     
                              </div>
                              
                              <%-- Users in Group list --%>
                              <div style="padding:4px">
                              
                              <a:panel id="users-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.users}">
                              
                              <a:richList id="users-list" binding="#{GroupsProperties.usersRichList}" viewMode="#{GroupsProperties.viewMode}" pageSize="12"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{GroupsDialog.users}" var="r" initialSortColumn="name" initialSortDescending="true">
                                 
                                 <%-- Primary column for icons view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;font-weight: bold;" rendered="#{GroupsProperties.viewMode == 'icons'}">
                                    <f:facet name="large-icon">
                                       <h:graphicImage alt="#{r.name}" value="/images/icons/user_large.gif" />
                                    </f:facet>
                                    <h:outputText value="#{r.name}" />
                                 </a:column>
                                 
                                 <%-- Primary column for details view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;" rendered="#{GroupsProperties.viewMode == 'details'}">
                                    <f:facet name="small-icon">
                                       <h:graphicImage alt="#{r.name}" value="/images/icons/person.gif" />
                                    </f:facet>
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.name}" />
                                 </a:column>
                                 
                                 <%-- Username column --%>
                                 <a:column width="120" style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.username}" value="userName" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.userName}" />
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.remove}" image="/images/icons/remove_user.gif" showLink="false" styleClass="inlineAction" actionListener="#{GroupsDialog.removeUser}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <a:dataPager/>
                              </a:richList>
                              
                              </a:panel>
                     
                              <div>
                              
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="dialog:close" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
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

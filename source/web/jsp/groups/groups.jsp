<%--
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
   
   <h:form acceptCharset="UTF-8" id="groups">
   
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
                                 <h:outputText value="#{msg.root_groups}" rendered="#{GroupsBean.currentGroup == null}" />
                                 <h:outputText value="#{GroupsBean.groupName}" rendered="#{GroupsBean.currentGroup != null}" />
                              </div>
                              <div class="mainSubText"><h:outputText value="#{msg.groups_description}" /></div>
                           </td>
                           
                           <td align=right>
                              <%-- Create actions menu --%>
                              <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:booleanEvaluator value="#{GroupsBean.currentGroup == null}">
                                    <a:actionLink value="#{msg.new_group}" image="/images/icons/create_group.gif" action="newGroup" actionListener="#{GroupsBean.clearGroupAction}" />
                                 </a:booleanEvaluator>
                                 <a:booleanEvaluator value="#{GroupsBean.currentGroup != null}">
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" action="newGroup" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsBean.currentGroup}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </a:menu>
                           </td>
                           <td style="padding-left:4px" width=80>
                              <%-- More actions menu --%>
                              <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:booleanEvaluator value="#{GroupsBean.currentGroup != null}">
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" action="newGroup" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsBean.currentGroup}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete_group}" image="/images/icons/delete_group.gif" action="deleteGroup" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsBean.currentGroup}" />
                                    </a:actionLink>
                                    <%-- TODO: should this be add user(S) - multiple required on generic picker? --%>
                                    <a:actionLink value="#{msg.add_user}" image="/images/icons/add_user.gif" action="addUser" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{GroupsBean.currentGroup}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </a:menu>
                           </td>
                          <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                          <td style="padding-left:4px" width=80 valign=middle>
                              <%-- Filter settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/filter.gif"
                                    value="#{GroupsBean.filterMode}" actionListener="#{GroupsBean.filterModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="children" label="#{msg.group_filter_children}" />
                                 <a:listItem value="all" label="#{msg.group_filter_all}" />
                              </a:modeList>
                           </td>
                          <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                          <td style="padding-left:4px" width=80 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{GroupsBean.viewMode}" actionListener="#{GroupsBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
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
                                 <a:breadcrumb value="#{GroupsBean.location}" styleClass="title" />
                              </div>
                              
                              <%-- Groups List --%>
                              <div style="padding:4px">
                              
                              <a:panel id="groups-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.groups}">
                              
                              <a:richList id="groups-list" binding="#{GroupsBean.groupsRichList}" viewMode="#{GroupsBean.viewMode}" pageSize="12"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{GroupsBean.groups}" var="r" initialSortColumn="name" initialSortDescending="true">
                                 
                                 <%-- Primary column for icons view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top" rendered="#{GroupsBean.viewMode == 'icons'}">
                                    <f:facet name="large-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/group_large.gif" actionListener="#{GroupsBean.clickGroup}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" actionListener="#{GroupsBean.clickGroup}" styleClass="header">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Primary column for details view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left" rendered="#{GroupsBean.viewMode == 'details'}">
                                    <f:facet name="small-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/group.gif" actionListener="#{GroupsBean.clickGroup}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.identifier}" value="name" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" actionListener="#{GroupsBean.clickGroup}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.new_sub_group}" image="/images/icons/create_group.gif" showLink="false" styleClass="inlineAction" action="newGroup" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.add_user}" image="/images/icons/add_user.gif" showLink="false" styleClass="inlineAction" action="addUser" actionListener="#{GroupsBean.setupGroupAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete}" image="/images/icons/delete_group.gif" showLink="false" styleClass="inlineAction" action="deleteGroup" actionListener="#{GroupsBean.setupGroupAction}">
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
                              
                              <a:richList id="users-list" binding="#{GroupsBean.usersRichList}" viewMode="#{GroupsBean.viewMode}" pageSize="12"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{GroupsBean.users}" var="r" initialSortColumn="name" initialSortDescending="true">
                                 
                                 <%-- Primary column for icons view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;font-weight: bold;" rendered="#{GroupsBean.viewMode == 'icons'}">
                                    <f:facet name="large-icon">
                                       <h:graphicImage alt="#{r.name}" value="/images/icons/user_large.gif" />
                                    </f:facet>
                                    <h:outputText value="#{r.name}" />
                                 </a:column>
                                 
                                 <%-- Primary column for details view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;" rendered="#{GroupsBean.viewMode == 'details'}">
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
                                    <a:actionLink value="#{msg.remove}" image="/images/icons/remove_user.gif" showLink="false" styleClass="inlineAction" actionListener="#{GroupsBean.removeUser}">
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
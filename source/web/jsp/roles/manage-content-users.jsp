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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_content_users">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="users">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/users_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.manage_content_users}" /> '<h:outputText value='#{BrowseBean.document.name}' />'</div>
                              <div class="mainSubText">
                                 <h:outputFormat value="#{msg.content_owner}" rendered="#{ContentUsersBean.owner != null}">
                                    <f:param value="#{ContentUsersBean.owner}" />
                                 </h:outputFormat>
                              </div>
                              <div class="mainSubText"><h:outputText value="#{msg.manage_content_users_description}" /></div>
                           </td>
                           
                           <td align=right>
                              <%-- Current object actions --%>
                              <r:permissionEvaluator value="#{BrowseBean.document}" allow="ChangePermissions">
                                 <a:actionLink value="#{msg.invite}" image="/images/icons/invite.gif" padding="4" action="wizard:inviteContentUsers" />
                              </r:permissionEvaluator>
                           </td>
                           
                           <%-- View mode settings
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                           <td width=100 valign=middle>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif" value="0" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="0" label="#{msg.user_details}" />
                              </a:modeList>
                           </td>
                           --%>
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
                     <table cellspacing="2" cellpadding="2" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              
                              <a:panel id="users-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.users_and_groups}">
                              
                              <a:richList id="users-list" binding="#{ContentUsersBean.usersRichList}" viewMode="details" pageSize="10"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{ContentUsersBean.users}" var="r" initialSortColumn="userName" initialSortDescending="true">
                                 
                                 <%-- Primary column with full name --%>
                                 <a:column primary="true" width="200" style="padding:2px;text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.name}" value="fullName" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <f:facet name="small-icon">
                                       <h:graphicImage url="#{r.icon}" />
                                    </f:facet>
                                    <h:outputText value="#{r.fullName}" />
                                 </a:column>
                                 
                                 <%-- Username column --%>
                                 <a:column width="120" style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.authority}" value="userName" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.userName}" />
                                 </a:column>
                                 
                                 <%-- Roles column --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.roles}" value="roles" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.roles}" />
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.change_roles}" image="/images/icons/edituser.gif" showLink="false" action="editRoles" actionListener="#{ContentUsersBean.setupUserAction}">
                                       <f:param name="userName" value="#{r.userName}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.remove}" image="/images/icons/delete_person.gif" showLink="false" action="removeUser" actionListener="#{ContentUsersBean.setupUserAction}">
                                       <f:param name="userName" value="#{r.userName}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <a:dataPager styleClass="pager" />
                              </a:richList>
                              
                              </a:panel>
                              
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="0" cellspacing="0" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" actionListener="#{ContentUsersBean.close}" action="dialog:close" styleClass="wizardButton" />
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
               
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="2" cellpadding="0" border="0" width="100%">
                        <tr>
                           <td>
                              <h:selectBooleanCheckbox id="chkPermissions" value="#{ContentUsersBean.inheritPermissions}" valueChangeListener="#{ContentUsersBean.inheritPermissionsValueChanged}"
                                    onclick="document.forms['users'].submit(); return true;" disabled="#{!ContentUsersBean.hasChangePermissions}" />
                           </td>
                           <td width=100%>
                              &nbsp;<h:outputText value="#{msg.inherit_permissions}" />
                           </td>
                        </tr>
                        <tr>
                           <td colspan=2><h:message for="chkPermissions" styleClass="statusMessage" /></td>
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
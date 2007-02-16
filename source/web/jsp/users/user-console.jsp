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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_user_console">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="user-console">
   
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
                           <td width=32>
                              <h:graphicImage id="logo" url="/images/icons/user_console_large.gif" width="32" height="32" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.user_console_info}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.user_console_description}" /></div>
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
                  <td valign=top>
                     
                     <table cellspacing=0 cellpadding=3 border=0 width=100%>
                        <tr>
                           <td width="100%" valign="top">
                              <%-- wrapper comment used by the panel to add additional component facets --%>
                              <h:panelGroup id="mydetails-panel-facets">
                                 <f:facet name="title">
                                    <a:actionLink value="#{msg.modify}" action="dialog:editUserDetails" showLink="false" image="/images/icons/Change_details.gif" rendered="#{NavigationBean.isGuest == false}" />
                                 </f:facet>
                              </h:panelGroup>
                              <a:panel label="#{msg.my_details}" id="mydetails-panel" facetsId="mydetails-panel-facets"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
                                 <table cellspacing=2 cellpadding=2 border=0>
                                    <tr>
                                       <td class="propertiesLabel">
                                          <h:outputText value="#{msg.first_name}" />:
                                       </td>
                                       <td>
                                          <h:outputText value="#{UsersBean.person.properties.firstName}" />
                                       </td>
                                    </tr>
                                    <tr>
                                       <td class="propertiesLabel">
                                          <h:outputText value="#{msg.last_name}" />:
                                       </td>
                                       <td>
                                          <h:outputText value="#{UsersBean.person.properties.lastName}" />
                                       </td>
                                    </tr>
                                    <tr>
                                       <td class="propertiesLabel">
                                          <h:outputText value="#{msg.email}" />:
                                       </td>
                                       <td>
                                          <h:outputText value="#{UsersBean.person.properties.email}" />
                                       </td>
                                    </tr>
                                 </table>
                                 <div style="padding:4px"></div>
                                 <%-- context for current user is setup on entry to user console --%>
                                 <a:actionLink id="change-password" value="#{msg.change_password}" action="dialog:changePassword" image="/images/icons/change_password.gif" rendered="#{NavigationBean.isGuest == false}" />
                              </a:panel>
                              
                              <div style="padding:4px"></div>
                              
                              <a:panel label="#{msg.general_pref}" id="pref-panel" rendered="#{NavigationBean.isGuest == false || UserPreferencesBean.allowGuestConfig == true}"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
                                 <table cellspacing=2 cellpadding=2 border=0>
                                    <tr>
                                       <td>
                                          <h:outputText value="#{msg.start_location}" />:&nbsp;
                                       </td>
                                       <td>
                                          <%-- Start Location drop-down selector --%>
                                          <h:selectOneMenu id="start-location" value="#{UserPreferencesBean.startLocation}" onchange="document.forms['user-console'].submit(); return true;">
                                             <f:selectItems value="#{UserPreferencesBean.startLocations}" />
                                          </h:selectOneMenu>
                                       </td>
                                    </tr>
                                    <tr>
                                       <td>
                                          <h:outputText value="#{msg.interface_language}" />:&nbsp;
                                       </td>
                                       <td>
                                          <%-- Interface Language drop-down selector --%>
                                          <h:selectOneMenu id="language" value="#{UserPreferencesBean.language}" onchange="document.forms['user-console'].submit(); return true;">
                                             <f:selectItems value="#{UserPreferencesBean.languages}" />
                                          </h:selectOneMenu>
                                       </td>
                                    </tr>
                                    <tr>
                                       <td>
                                          <h:outputText value="#{msg.content_language_filter}" />:&nbsp;
                                       </td>
                                       <td>
                                          <%-- Content Language Filter drop-down selector --%>
                                          <h:selectOneMenu id="content-filter-language" value="#{UserPreferencesBean.contentFilterLanguage}" onchange="document.forms['user-console'].submit(); return true;">
                                             <f:selectItems value="#{UserPreferencesBean.contentFilterLanguages}" />
                                          </h:selectOneMenu>
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                              <div style="padding:4px"></div>
                              
                              <a:panel label="#{msg.user_management}" id="man-panel" rendered="#{NavigationBean.isGuest == false}"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white">
                                 <table cellspacing=2 cellpadding=2 border=0>
                                    <tr>
                                       <td>
                                          <a:actionLink id="manage-deleted-items" value="#{msg.manage_deleted_items}" action="dialog:manageDeletedItems" image="/images/icons/trashcan.gif" />
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
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
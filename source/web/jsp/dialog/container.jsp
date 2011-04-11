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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<%
if (Application.getDialogManager().getState() == null)
{
   response.sendRedirect(request.getContextPath() + "/faces/jsp/dashboards/container.jsp");
   return;
}
%>

<r:page title="<%=Application.getDialogManager().getTitle() %>">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="dialog">
   
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
                              <h:graphicImage id="dialog-logo" url="#{DialogManager.icon}" />
                           </td>
                           <td width="100%">
                              <div class="mainTitle"><h:outputText value="#{DialogManager.title}" /></div>
                              <div class="mainSubTitle"><h:outputText value="#{DialogManager.subTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{DialogManager.description}" /></div>
                           </td>
                           <td>
                              <table cellspacing="4" cellpadding="1" width="100%">
                                 <tr>
                                    <%-- Main actions --%>
                                    <a:panel id="main-actions-panel" rendered="#{DialogManager.actionsId != null}">
                                       <td style="white-space: nowrap;">
                                          <r:actions id="main_actions_list" rendered="#{DialogManager.actionsAsMenu == false}"
                                                     styleClass="dialogMainActions" value="#{DialogManager.actionsId}" 
                                                     context="#{DialogManager.actionsContext}" />
                                                     
                                          <a:menu id="main_actions_menu" rendered="#{DialogManager.actionsAsMenu == true}"
                                                  itemSpacing="4" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" 
                                                  style="white-space: nowrap" label="#{DialogManager.actionsMenuLabel}">
                                             <r:actions id="main_actions_menu_items" value="#{DialogManager.actionsId}" 
                                                        context="#{DialogManager.actionsContext}" />
                                          </a:menu>
                                       </td>
                                    </a:panel>
                                    
                                    <%-- More actions menu --%>
                                    <a:panel id="more-actions-panel" rendered="#{DialogManager.moreActionsId != null}">
                                       <td style="padding-left: 4px" width="80">
                                          <a:menu id="more_actions_menu" itemSpacing="4" image="/images/icons/menu.gif" 
                                                  menuStyleClass="moreActionsMenu" style="white-space:nowrap" 
                                                  label="#{DialogManager.moreActionsMenuLabel}">
                                             <r:actions id="more_actions_menu_items" value="#{DialogManager.moreActionsId}" 
                                                        context="#{DialogManager.actionsContext}" />
                                          </a:menu>
                                       </td>
                                    </a:panel>
                                    
                                    <%-- View Filters --%>
                                    <a:panel id="filters-panel" rendered="#{DialogManager.filterListVisible}">
                                       <td class="separator" width="1">
                                          <img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border="0" height="29" width="1" />
                                       </td>
                                       <td style="padding-left: 4px" width="80" valign="middle">
                                          <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" 
                                                      selectedImage="/images/icons/filter.gif" menu="true" styleClass="moreActionsMenu" 
                                                      menuImage="/images/icons/menu.gif" value="#{DialogManager.bean.filterMode}" 
                                                      actionListener="#{DialogManager.bean.filterModeChanged}">
                                             <a:listItems value="#{DialogManager.bean.filterItems}" />
                                          </a:modeList>
                                       </td>
                                    </a:panel>
                                    
                                    <%-- View Mode --%>
                                    <a:panel id="views-panel" rendered="#{DialogManager.viewListVisible}">
                                       <td class="separator" width="1">
                                          <img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border="0" height="29" width="1" />
                                       </td>
                                       <td style="padding-left: 4px" width="80" valign="middle">
                                          <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" 
                                                      selectedImage="/images/icons/Details.gif" menu="true" styleClass="moreActionsMenu" 
                                                      menuImage="/images/icons/menu.gif" value="#{DialogManager.bean.viewMode}" 
                                                      actionListener="#{DialogManager.bean.viewModeChanged}">
                                             <a:listItems value="#{DialogManager.bean.viewItems}" />
                                          </a:modeList>
                                       </td>
                                    </a:panel>
                                    
                                    <%-- Navigation --%>
                                    <a:panel id="nav-panel" rendered="#{DialogManager.navigationVisible}">
                                       <td style="padding-left: 10px; white-space: nowrap;" valign="middle">
                                          <a:actionLink id="act-prev" value="#{msg.previous_item}" verticalAlign="-8px" image="/images/icons/nav_prev.gif" 
                                                        showLink="false" actionListener="#{DialogManager.bean.previousItem}" action="#{DialogManager.bean.getOutcome}">
                                             <f:param name="id" value="#{DialogManager.bean.currentItemId}" />
                                          </a:actionLink>
                                          <img src="<%=request.getContextPath()%>/images/icons/nav_file.gif" width="24" height="24" align="absmiddle" />
                                          <a:actionLink id="act-next" value="#{msg.next_item}" verticalAlign="-8px" image="/images/icons/nav_next.gif" 
                                                        showLink="false" actionListener="#{DialogManager.bean.nextItem}" action="#{DialogManager.bean.getOutcome}">
                                             <f:param name="id" value="#{DialogManager.bean.currentItemId}" />
                                          </a:actionLink>
                                       </td>
                                    </a:panel>

                                    <td><div style="width: 5px;">&nbsp;</div></td>
                                 </tr>
                              </table>
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
                           
                              <a:errors message="#{DialogManager.errorMessage}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <f:subview id="dialog-body">
                                 <h:outputText id="validation_invalid_character" style="display:none" value="#{msg.validation_invalid_character}" />
                                 <jsp:include page="<%=Application.getDialogManager().getPage() %>" />
                              </f:subview>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <r:dialogButtons id="dialog-buttons" styleClass="wizardButton" />
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
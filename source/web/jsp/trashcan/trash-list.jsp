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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_deleted_items">

<script language="JavaScript1.2">

   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("trashcan:search-text").focus();
      updateButtonState();
   }
   
   function updateButtonState()
   {
      if (document.getElementById("trashcan:search-text").value.length == 0)
      {
         document.getElementById("trashcan:search-btn1").disabled = true;
         document.getElementById("trashcan:search-btn2").disabled = true;
      }
      else
      {
         document.getElementById("trashcan:search-btn1").disabled = false;
         document.getElementById("trashcan:search-btn2").disabled = false;
      }
   }
   
   function userSearch(e)
   {
      var keycode;
      if (window.event) keycode = window.event.keyCode;
      else if (e) keycode = e.which;
      if (keycode == 13)
      {	
      	document.forms['trashcan']['trashcan:modelist'].value='trashcan:user-filter:user';
         document.forms['trashcan'].submit();
         return false;
      }
      return true;
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="trashcan">
   
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
                              <h:graphicImage id="logo" url="/images/icons/trashcan_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.manage_deleted_items}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.manage_deleted_items_description}" /></div>
                           </td>
                           
                           <td align=right style="white-space:nowrap">
                              <%-- Admin only global actions --%>
                              <a:actionLink value="#{msg.recover_all_items}" image="/images/icons/recover_all.gif" action="dialog:recoverAllItems" actionListener="#{TrashcanBean.setupListAction}" rendered="#{NavigationBean.currentUser.admin == true}" />&nbsp;
                              <a:actionLink value="#{msg.delete_all_items}" image="/images/icons/delete_all.gif" action="dialog:deleteAllItems" actionListener="#{TrashcanBean.setupListAction}" rendered="#{NavigationBean.currentUser.admin == true}" />
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
                     <table cellspacing="2" cellpadding="2" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              
                              <%-- Deleted Items List --%>
                              <a:panel id="trashcan-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{TrashcanBean.panelMessage}">
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                              <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                 <tr>
                                    <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                    <td class="mainSubText">
                                       <h:outputText value="#{msg.deleted_items_info}" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                              
                              <%-- Search controls --%>
                              <div style="padding: 4px;"></div>
                              <h:inputText id="search-text" value="#{TrashcanBean.searchText}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />&nbsp;
                              <h:commandButton id="search-btn1" value="#{msg.search_deleted_items_name}" actionListener="#{TrashcanBean.searchName}" disabled="true" />&nbsp;
                              <h:commandButton id="search-btn2" value="#{msg.search_deleted_items_text}" actionListener="#{TrashcanBean.searchContent}" disabled="true" />&nbsp;
                              <h:commandButton id="clear-btn" value="#{msg.show_all}" actionListener="#{TrashcanBean.clearSearch}" />
                              <div style="padding: 4px;"></div>
                              
                              <%-- Filter controls --%>
                              <table cellspacing=2 cellpadding=0 width=100%>
                                 <tr>
                                    <td class="filterBorders" width=100%>
                                       <table cellspacing=2 cellpadding=0 width=100%>
                                          <tr>
                                             <td><img src="<%=request.getContextPath()%>/images/icons/filter.gif" width=16 height=16></td>
                                             <td style="padding-left:8px;width:120px"><nobr><h:outputText value="#{msg.date_filter_when}" />:</nobr></td>
                                             <td width=100%>
                                                <a:modeList itemSpacing="2" iconColumnWidth="0" horizontal="true" selectedLinkStyle="font-weight:bold"
                                                      value="#{TrashcanBean.dateFilter}" actionListener="#{TrashcanBean.dateFilterChanged}">
                                                   <a:listItem value="all" label="#{msg.date_filter_all}" />
                                                   <a:listItem value="today" label="#{msg.date_filter_today}" />
                                                   <a:listItem value="week" label="#{msg.date_filter_week}" />
                                                   <a:listItem value="month" label="#{msg.date_filter_month}" />
                                                </a:modeList>
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 <%-- Only the admin user needs the username filter --%>
                                 <a:panel id="userfilter-panel" rendered="#{NavigationBean.currentUser.admin == true}">
                                 <tr>
                                    <td class="filterBorders" width=100%>
                                       <table cellspacing=2 cellpadding=0 width=100%>
                                          <tr>
                                             <td><img src="<%=request.getContextPath()%>/images/icons/filter.gif" width=16 height=16></td>
                                             <td style="padding-left:8px;width:120px"><nobr><h:outputText value="#{msg.user_filter_who}" />:</nobr></td>
                                             <td width=100%>
                                                <table cellspacing=0 cellpadding=0 border=0><tr><td>
                                                <a:modeList id="user-filter" itemSpacing="2" iconColumnWidth="0" horizontal="true" selectedLinkStyle="font-weight:bold"
                                                      value="#{TrashcanBean.userFilter}" actionListener="#{TrashcanBean.userFilterChanged}">
                                                   <a:listItem value="all" label="#{msg.user_filter_all}" />
                                                   <a:listItem value="user" label="#{msg.user_filter_user}" />
                                                </a:modeList>
                                                </td><td>
                                                <h:inputText id="user-search" value="#{TrashcanBean.userSearchText}" size="12" maxlength="100" style="font-size:10px" onkeyup="return userSearch(event);" />
                                                </td></tr></table>
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 </a:panel>
                              </table>
                              
                              <div style="padding: 4px;"></div>
                              
                              <%-- Recover Listed Items actions --%>
                              <a:actionLink value="#{msg.recover_listed_items}" image="/images/icons/recover_all.gif" action="dialog:recoverListedItems" actionListener="#{TrashcanBean.setupListAction}" />&nbsp;
                              <a:actionLink value="#{msg.delete_listed_items}" image="/images/icons/delete_all.gif" action="dialog:deleteListedItems" actionListener="#{TrashcanBean.setupListAction}" />
                              <div style="padding: 4px;"></div>
                              
                              <a:richList id="trashcan-list" binding="#{TrashcanBean.itemsRichList}" viewMode="details" pageSize="10"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{TrashcanBean.items}" var="r" initialSortColumn="deletedDate" initialSortDescending="true">
                                 
                                 <%-- Primary column showing item name --%>
                                 <a:column primary="true" width="150" style="padding:2px;text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <f:facet name="small-icon">
                                       <a:actionLink value="#{r.name}" action="dialog:itemDetails" actionListener="#{TrashcanBean.setupItemAction}" image="#{r.typeIcon}" showLink="false" styleClass="inlineAction">
                                         <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" action="dialog:itemDetails" actionListener="#{TrashcanBean.setupItemAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Original Location Path column --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.original_location}" value="displayPath" styleClass="header"/>
                                    </f:facet>
                                    <r:nodePath value="#{r.locationPath}" actionListener="#{BrowseBean.clickSpacePath}" showLeaf="true" />
                                 </a:column>
                                 
                                 <%-- Deleted Date column --%>
                                 <a:column style="text-align:left;white-space:nowrap">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.deleted_date}" value="deletedDate" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.deletedDate}">
                                       <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                                    </h:outputText>
                                 </a:column>
                                 
                                 <%-- Deleted by user column --%>
                                 <a:column width="120" style="text-align:left" rendered="#{NavigationBean.currentUser.admin == true}">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.deleted_user}" value="deletedBy" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.deletedBy}" />
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.recover}" image="/images/icons/recover.gif" showLink="false" action="dialog:recoverItem" actionListener="#{TrashcanBean.setupItemAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif" showLink="false" action="dialog:deleteItem" actionListener="#{TrashcanBean.setupItemAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <a:dataPager styleClass="pager" />
                              </a:richList>
                              
                              <h:message for="trashcan-list" styleClass="statusMessage" />
                              
                              </a:panel>
                              
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="0" cellspacing="0" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="#{TrashcanBean.close}" styleClass="wizardButton" />
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
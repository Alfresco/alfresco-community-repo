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

<r:page titleId="title_categories_list">

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
                              <h:graphicImage id="logo" url="/images/icons/category.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.category_management}" /></div>
                              <div class="mainSubTitle">
                                 <%-- show either root message or the current category name --%>
                                 <h:outputText value="#{msg.categories}" rendered="#{CategoriesBean.currentCategoryId == null}" />
                                 <h:outputText value="#{CategoriesBean.currentCategory.name}" rendered="#{CategoriesBean.currentCategoryId != null}" />
                              </div>
                              <div class="mainSubText"><h:outputText value="#{msg.categories_description}" /></div>
                           </td>
                           
                           <td style="padding-left:4px" width=52>
                              <%-- Create actions menu --%>
                              <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:actionLink value="#{msg.add_category}" image="/images/icons/add_category.gif" action="addCategory" actionListener="#{CategoriesBean.clearCategoryAction}" />
                              </a:menu>
                           </td>
                           <td style="padding-left:4px" width=80>
                              <%-- More actions menu --%>
                              <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <a:booleanEvaluator value="#{CategoriesBean.currentCategoryId != null}">
                                    <a:actionLink value="#{msg.edit_category}" image="/images/icons/edit_category.gif" action="editCategory" actionListener="#{CategoriesBean.setupCategoryAction}">
                                       <f:param name="id" value="#{CategoriesBean.currentCategoryId}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete_category}" image="/images/icons/delete_category.gif" action="deleteCategory" actionListener="#{CategoriesBean.setupCategoryAction}">
                                       <f:param name="id" value="#{CategoriesBean.currentCategoryId}" />
                                    </a:actionLink>
                                 </a:booleanEvaluator>
                              </a:menu>
                           </td>
                           
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>
                           <td width=110 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{CategoriesBean.viewMode}" actionListener="#{CategoriesBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="icons" label="#{msg.category_icons}" />
                                 <a:listItem value="details" label="#{msg.category_details}" />
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
                              
                              <%-- Category Path Breadcrumb --%>
                              <div style="padding-left:8px;padding-top:4px;padding-bottom:4px">
                                 <a:breadcrumb value="#{CategoriesBean.location}" styleClass="title" />
                              </div>
                              
                              <%-- Categories List --%>
                              <div style="padding:4px">
                              
                              <a:panel id="categories-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.items}">
                              
                              <a:richList id="categories-list" binding="#{CategoriesBean.categoriesRichList}" viewMode="#{CategoriesBean.viewMode}" pageSize="15"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{CategoriesBean.categories}" var="r" initialSortColumn="name" initialSortDescending="true">
                                 
                                 <%-- Primary column for icons view mode --%>
                                 <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <f:facet name="large-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/category.gif" actionListener="#{CategoriesBean.clickCategory}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <f:facet name="small-icon">
                                       <a:actionLink value="#{r.name}" image="/images/icons/category_small.gif" actionListener="#{CategoriesBean.clickCategory}" showLink="false">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </f:facet>
                                    <a:actionLink value="#{r.name}" actionListener="#{CategoriesBean.clickCategory}" styleClass="header">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.modify}" image="/images/icons/edit_category.gif" showLink="false" action="editCategory" style="padding-right:2px" actionListener="#{CategoriesBean.setupCategoryAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.delete}" image="/images/icons/delete_category.gif" showLink="false" action="deleteCategory" actionListener="#{CategoriesBean.setupCategoryAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                 </a:column>
                                 
                                 <a:dataPager styleClass="pager" />
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
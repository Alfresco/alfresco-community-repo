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

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_website_details">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="website-details">
   
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
                              <img src="<%=request.getContextPath()%>/images/icons/details_large.gif" width=32 height=32>
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.details_of}" /> '<h:outputText value="#{SpaceDetailsBean.name}" />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.location}" />: <r:nodePath value="#{SpaceDetailsBean.space.nodeRef}" breadcrumb="true" actionListener="#{BrowseBean.clickSpacePath}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.websitedetails_description}" /></div>
                           </td>
                           
                           <%-- Navigation --%>
                           <td align="right">
                              <a:actionLink value="#{msg.previous_item}" image="/images/icons/nav_prev.gif" showLink="false" actionListener="#{SpaceDetailsBean.previousItem}" action="showSpaceDetails">
                                 <f:param name="id" value="#{SpaceDetailsBean.id}" />
                              </a:actionLink>
                              <img src="<%=request.getContextPath()%>/images/icons/nav_space.gif" width=24 height=24 align=absmiddle>
                              <a:actionLink value="#{msg.next_item}" image="/images/icons/nav_next.gif" showLink="false" actionListener="#{SpaceDetailsBean.nextItem}" action="showSpaceDetails">
                                 <f:param name="id" value="#{SpaceDetailsBean.id}" />
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
                              
                              <a:panel label="#{msg.links}" id="links-panel" progressive="true"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
                                       expanded='#{SpaceDetailsBean.panels["links-panel"]}' expandedActionListener="#{SpaceDetailsBean.expandPanel}">
                                 <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
                                    <tr>
                                       <td>
                                          <a:actionLink value="#{msg.view_in_webdav}" href="#{SpaceDetailsBean.webdavUrl}" target="new" id="link1" />
                                       </td>
                                       <td>
                                          <a:actionLink value="#{msg.view_in_cifs}" href="#{SpaceDetailsBean.cifsPath}" target="new" id="link2" />
                                       </td>
                                       <td>
                                          <a href='<%=request.getContextPath()%><a:outputText value="#{SpaceDetailsBean.bookmarkUrl}" id="out1" />' onclick="return false;"><a:outputText value="#{msg.details_page_bookmark}" id="out2" /></a>
                                       </td>
                                       <td>
                                          <a href='<a:outputText value="#{SpaceDetailsBean.nodeRefUrl}" id="out3" />' onclick="return false;"><a:outputText value="#{msg.noderef_link}" id="out4" /></a>
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                              <div style="padding:4px"></div>
                              
                              <%-- wrapper comment used by the panel to add additional component facets --%>
                              <h:panelGroup id="props-panel-facets">
                                 <f:facet name="title">
                                    <r:permissionEvaluator value="#{SpaceDetailsBean.space}" allow="Write">
                                       <a:actionLink id="titleLink1" value="#{msg.modify}" showLink="false" image="/images/icons/edit_properties.gif"
                                             action="dialog:editSpace" />
                                    </r:permissionEvaluator>
                                 </f:facet>
                              </h:panelGroup>
                              <a:panel label="#{msg.properties}" id="properties-panel" facetsId="props-panel-facets"
                                       border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true"
                                       expanded='#{SpaceDetailsBean.panels["properties-panel"]}' expandedActionListener="#{SpaceDetailsBean.expandPanel}">
                                 <table cellspacing="0" cellpadding="0" border="0" width="100%">
                                    <tr>
                                       <td width=80 align=center>
                                          <%-- icon image for the space --%>
                                          <table cellspacing=0 cellpadding=0 border=0>
                                             <tr>
                                                <td>
                                                   <div style="border: thin solid #CCCCCC; padding:4px">
                                                      <h:graphicImage id="space-logo" url="/images/icons/#{SpaceDetailsBean.space.properties.icon}.gif" width="32" height="32" />
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
                                          <%-- properties for the space --%>
                                          <r:propertySheetGrid id="space-props" value="#{SpaceDetailsBean.space}" var="spaceProps" 
                                                         columns="1" mode="view" labelStyleClass="propertiesLabel" 
                                                         externalConfig="true" />
                                          <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
                                          <h:message for="space-props" styleClass="statusMessage" />
                                       </td>
                                    </tr>
                                 </table>
                              </a:panel>
                              
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0" width="100%">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="#{SpaceDetailsBean.closeDialog}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                              
                              <div style="padding:4px"></div>
                              
                              <%-- Website Actions --%>
                              <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true"
                                    expanded='#{SpaceDetailsBean.panels["actions-panel"]}' expandedActionListener="#{SpaceDetailsBean.expandPanel}">
                                 <r:actions id="actions_space" value="website_details_actions" context="#{SpaceDetailsBean.space}" verticalSpacing="3" style="white-space:nowrap" />
                              </a:panel>
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
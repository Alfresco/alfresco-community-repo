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

<r:page titleId="title_edit_file">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%-- set the form name here --%>
   <h:form acceptCharset="UTF-8" id="edit-file">
   
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
                           <td width="26">
                              <h:graphicImage id="wizard-logo" url="/images/icons/edit_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle">'<h:outputText value="#{CheckinCheckoutBean.document.name}" />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.editfile_description}" /></div>
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
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td class="mainSubTitle">
                                       <h:outputText value="#{msg.edit_file_title}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText">
                                       <h:outputFormat value="#{msg.edit_file_prompt}">
                                          <f:param value="#{CheckinCheckoutBean.document.name}" />
                                       </h:outputFormat>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td style="padding:10px" valign="middle">
                                       <%-- downloadable file link generated by CheckinCheckoutBean --%>
                                       <a:actionLink styleClass="title" image="#{CheckinCheckoutBean.document.properties.fileType32}" value="#{CheckinCheckoutBean.document.name}" href="#{CheckinCheckoutBean.document.properties.url}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.edit_download_complete}" />
                                    </td>
                                 </tr>
                                 
                                 <%-- Hide the checkout info if this document is already checked out --%>
                                 <a:panel id="checkout-panel" rendered="#{CheckinCheckoutBean.document.properties.workingCopy == false}">
                                    <tr><td class="paddingRow"></td></tr>
                                    <tr>
                                       <td class="mainSubTitle">
                                          <h:outputText value="#{msg.checkout_file_title}" />
                                       </td>
                                    </tr>
                                    <tr>
                                       <td>
                                          <h:outputText value="#{msg.checkout_you_may_want}" />
                                          <div class="mainSubText" style="padding-top:6px;padding-left:2px">
                                             <a:actionLink value="#{msg.checkout}" image="/images/icons/CheckOut_icon.gif" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkoutFile">
                                                <f:param name="id" value="#{CheckinCheckoutBean.document.id}" />
                                             </a:actionLink>
                                          </div>
                                          <div class="mainSubText" style="padding-top:6px"><h:outputText value="#{msg.checkout_hint1}" /></div>
                                          <div class="mainSubText"><h:outputText value="#{msg.checkout_hint2}" /></div>
                                       </td>
                                    </tr>
                                 </a:panel>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="#{CheckinCheckoutBean.editFileOK}" styleClass="dialogControls" />
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
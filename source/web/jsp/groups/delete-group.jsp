<%--
 Copyright (C) 2005 Alfresco, Inc.

 Licensed under the Alfresco Network License. You may obtain a
 copy of the License at

   http://www.alfrescosoftware.com/legal/

 Please view the license relevant to your network subscription.

 BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_delete_group">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%-- set the form name here --%>
   <h:form acceptCharset="UTF-8" id="delete-group">
   
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
                  <td bgcolor="#EEEEEE">
                     
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage url="/images/icons/delete_group_large.gif"/>
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.delete_group}" /> '<h:outputText value="#{GroupsBean.actionGroupName}" />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.delete_group_info}" /></div>
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
                     <table cellspacing="0" cellpadding="4" border="0" width="100%">
                        <tr>
                           
                           <td width="100%" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0">
                                 <a:panel id="delete-panel" rendered="#{GroupsBean.actionGroupItems != 0}">
                                 <tr>
                                    <td width="100%" valign="top">
                                       <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                                       <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                          <tr>
                                             <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                             <td class="mainSubText">
                                                <h:outputFormat value="#{msg.delete_group_warning}">
                                                   <f:param value="#{GroupsBean.actionGroupItems}" />
                                                </h:outputFormat>
                                             </td>
                                          </tr>
                                       </table>
                                       <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                                    </td>
                                 </tr>
                                 </a:panel>
                                 <tr>
                                    <td class="mainSubTitle">
                                       <h:outputFormat value="#{msg.delete_group_confirm}">
                                          <f:param value="#{GroupsBean.actionGroupName}"/>
                                       </h:outputFormat>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <%-- Error Messages --%>
                                       <%-- messages tag to show messages not handled by other specific message tags --%>
                                       <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
                                    <td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.delete}" action="#{GroupsBean.finishDelete}" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                                 <tr><td class="dialogButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="cancel" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
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

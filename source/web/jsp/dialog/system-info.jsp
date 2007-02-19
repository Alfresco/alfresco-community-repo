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

<r:page titleId="title_system_info">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   <f:loadBundle basename="alfresco.version" var="version"/>
   
   <h:form acceptCharset="UTF-8" id="system-information-form">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/file_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.system_info}" /></div>
                              <div class="mainSubTitle"><h:outputText value="#{msg.current_user}" />: <h:outputText value="#{NavigationBean.currentUser.userName}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.version}" />: <h:outputText value='#{version["version.edition"]} - v#{version["version.major"]}.#{version["version.minor"]}.#{version["version.revision"]}' /> <h:outputText rendered='#{version["version.label"] != ""}' value='(#{version["version.label"]})' /></div>
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
                                    <td colspan="2">
                                       <a:panel label="#{msg.http_app_state}" id="http-application-state" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:httpApplicationState id="has" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.http_session_state}" id="http-session-state" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:httpSessionState id="hss" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.http_request_state}" id="http-request-state" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:httpRequestState id="hrs" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.http_request_params}" id="http-request-params" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:httpRequestParams id="hrp" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.http_request_headers}" id="http-request-headers" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:httpRequestHeaders id="hrh" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.repository_props}" id="repo-props" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:repositoryProperties id="rp" />
                                       </a:panel>
                                       <br/>
                                       <a:panel label="#{msg.system_props}" id="system-props" border="white" bgcolor="white" 
                                                titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
                                                expanded="false">
                                       	<a:systemProperties id="sp" />
                                       </a:panel>
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
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
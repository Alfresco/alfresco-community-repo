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

<r:page titleId="title_export">

<script language="JavaScript1.2">
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("export-form:package-name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("export-form:package-name").value.length == 0 ||
          document.getElementById("export-form:destination_selected").value.length == 0)
      {
         document.getElementById("export-form:ok-button").disabled = true;
      }
      else
      {
         document.getElementById("export-form:ok-button").disabled = false;
      }
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="export-form">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/export_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.export}"/> '<h:outputText value='#{BrowseBean.actionSpace.name}' />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.export_info}"/></div>
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
                              
                              <a:errors message="#{msg.error_export_all}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td><nobr><h:outputText value="#{msg.package_name}"/>:</nobr></td>
                                    <td width="90%">
                                       <h:inputText id="package-name" value="#{ExportDialog.packageName}" size="35" maxlength="1024" 
                                                    onkeyup="javascript:checkButtonState();" />
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><nobr><h:outputText value="#{msg.destination}"/>:</nobr></td>
                                    <td>
                                       <r:spaceSelector id="destination" label="#{msg.select_destination_prompt}" 
                                                        value="#{ExportDialog.destination}" 
                                                        initialSelection="#{NavigationBean.currentNodeId}"
                                                        styleClass="selector"/>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><nobr><h:outputText value="#{msg.export_from}:" rendered="#{NavigationBean.currentUser.admin == true}"/></nobr></td>
                                    <td>
                                       <h:selectOneRadio value="#{ExportDialog.mode}" layout="pageDirection" rendered="#{NavigationBean.currentUser.admin == true}">
                                          <f:selectItem itemValue="all" itemLabel="#{msg.all_spaces_root}" />
                                          <f:selectItem itemValue="current" itemLabel="#{msg.current_space}" />
                                       </h:selectOneRadio>
                                    </td>
                                 </tr>
                                 <%--
                                 <tr>
                                    <td><nobr><h:outputText value="#{msg.encoding}"/>:</nobr></td>
                                    <td>
                                       <h:selectOneMenu value="#{ExportDialog.encoding}">
                                          <f:selectItems value="#{NewRuleWizard.encodings}" />
                                       </h:selectOneMenu>
                                    </td>
                                 </tr>
                                 --%>
                                 <tr>
                                    <td>&nbsp;</td>
                                    <td>
                                       <h:outputText value="<span style='padding-left: 24px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
                                       <h:selectBooleanCheckbox value="#{ExportDialog.includeChildren}"/>&nbsp;
                                       <span style="vertical-align:20%"><h:outputText value="#{msg.include_children}"/></span>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>&nbsp;</td>
                                    <td>
                                       <h:outputText value="<span style='padding-left: 24px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
                                       <h:selectBooleanCheckbox value="#{ExportDialog.includeSelf}"/>&nbsp;
                                       <span style="vertical-align:20%"><h:outputText value="#{msg.include_self}"/></span>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>&nbsp;</td>
                                    <td>
                                       <h:outputText value="<span style='padding-left: 5px'/>" escape="false" rendered="#{NavigationBean.currentUser.admin == true}"/>
                                       <h:selectBooleanCheckbox value="#{ExportDialog.runInBackground}" />&nbsp;
                                       <span style="vertical-align:20%"><h:outputText value="#{msg.run_export_in_background}"/></span>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>&nbsp;</td>
                                    <td>
                                       <div id="error-info" style="padding-left: 30px;">
                                          <h:graphicImage alt="" value="/images/icons/info_icon.gif" style="vertical-align: middle;"/>&nbsp;
                                          <h:outputText value="#{msg.export_error_info}"/>
                                       </div>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.ok}" action="#{ExportDialog.export}" 
                                                        disabled="true" styleClass="wizardButton"/>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="#{ExportDialog.cancel}" 
                                                        styleClass="wizardButton"/>
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
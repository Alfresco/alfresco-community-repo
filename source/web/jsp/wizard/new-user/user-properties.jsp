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
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_new_user_user_props">

<script type="text/javascript">

   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      if (document.getElementById("user-props:userName") != null &&
          document.getElementById("user-props:userName").disabled == false)
      {
         document.getElementById("user-props:userName").focus();
      }
      else
      {
         document.getElementById("user-props:homeSpaceName").focus();
      }   
      updateButtonState();
   }

   function updateButtonState()
   {
      if (document.getElementById("user-props:password") != null &&
          document.getElementById("user-props:password").disabled == false)
      {
         if (document.getElementById("user-props:userName").value.length == 0 ||
             document.getElementById("user-props:password").value.length == 0 ||
             document.getElementById("user-props:confirm").value.length == 0)
         {
            document.getElementById("user-props:finish-button").disabled = true;
            document.getElementById("user-props:next-button").disabled = true;
         }
         else
         {
            document.getElementById("user-props:finish-button").disabled = false;
            document.getElementById("user-props:next-button").disabled = false;
         }
      }
      else
      {
         document.getElementById("user-props:finish-button").disabled = false;
         document.getElementById("user-props:next-button").disabled = false;
      }
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <%-- set the form name here --%>
   <h:form acceptcharset="UTF-8" id="user-props">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2" width="100%">
      
      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="<h:outputText value="#{NavigationBean.workAreaWidth}" />">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/new_user_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{NewUserWizard.wizardTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{NewUserWizard.wizardDescription}" /></div>
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
                           <td width="20%" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <h:outputText styleClass="mainSubTitle" value="#{msg.steps}"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="2" selectedStyleClass="statusListHighlight"
                                    value="2" disabled="true">
                                 <a:listItem value="1" label="1. #{msg.person_properties}" />
                                 <a:listItem value="2" label="2. #{msg.user_properties}" />
                                 <a:listItem value="3" label="3. #{msg.summary}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                              
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan="2" class="mainSubTitle"><h:outputText value="#{NewUserWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td colspan="2" class="mainSubText"><h:outputText value="#{NewUserWizard.stepDescription}" /></td>
                                 </tr>
                                 
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.user_properties}"/></td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.username}"/>:</td>
                                    <td>
                                       <h:inputText id="userName" value="#{NewUserWizard.userName}" size="35" maxlength="1024" validator="#{LoginBean.validateUsername}" onkeyup="updateButtonState();" onchange="updateButtonState();" disabled="#{NewUserWizard.editMode}" />&nbsp;*
                                       &nbsp;<h:message id="errors1" for="userName" style="color:red" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.password}"/>:</td>
                                    <td>
                                       <h:inputSecret id="password" value="#{NewUserWizard.password}" size="35" maxlength="1024" validator="#{LoginBean.validatePassword}" onkeyup="updateButtonState();" onchange="updateButtonState();" disabled="#{NewUserWizard.editMode}" redisplay="true" />&nbsp;*
                                       &nbsp;<h:message id="errors2" for="password" style="color:red" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.confirm}"/>:</td>
                                    <td>
                                       <h:inputSecret id="confirm" value="#{NewUserWizard.confirm}" size="35" maxlength="1024" validator="#{LoginBean.validatePassword}" onkeyup="updateButtonState();" onchange="updateButtonState();" disabled="#{NewUserWizard.editMode}" redisplay="true" />&nbsp;*
                                       &nbsp;<h:message id="errors3" for="confirm" style="color:red" />
                                    </td>
                                 </tr>
                                 
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.homespace}"/></td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.home_space_location}"/>:</td>
                                    <td>
                                       <r:ajaxFolderSelector id="spaceSelector" label="#{msg.select_home_space_prompt}" 
                                                                      value="#{NewUserWizard.homeSpaceLocation}" 
                                                                      initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
                                                                      style="border: 1px dashed #cccccc; padding: 2px; display: inline;" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.home_space_name}"/>:</td>
                                    <td>
                                       <h:inputText id="homeSpaceName" value="#{NewUserWizard.homeSpaceName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
                                    </td>
                                 </tr>
                                 
                                 <a:panel id="home-info-panel" rendered="#{NewUserWizard.editMode}">
                                 <tr>
                                    <td colspan=2>
                                       <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                                       <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                          <tr>
                                             <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                             <td class="mainSubText">
                                                <h:outputText value="#{msg.user_change_homespace_info}" />
                                             </td>
                                          </tr>
                                       </table>
                                       <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                                    </td>
                                 </tr>
                                 </a:panel>
                                 
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2"><h:outputText value="#{NewUserWizard.stepInstructions}" /></td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.next_button}" id="next-button" action="#{NewUserWizard.next}" styleClass="wizardButton" disabled="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.back_button}" action="#{NewUserWizard.back}" styleClass="wizardButton" immediate="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.finish_button}" id="finish-button" action="#{NewUserWizard.finish}" styleClass="wizardButton" disabled="true" />
                                    </td>
                                 </tr>
                                 <tr><td class="wizardButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel_button}" id="cancel-button" action="#{NewUserWizard.cancel}" styleClass="wizardButton" immediate="true" />
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
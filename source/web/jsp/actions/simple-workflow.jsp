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

<r:page titleId="title_action_simple_workflow">

<script type="text/javascript">
   
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("simple-workflow-action:approve-step-name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("simple-workflow-action:approve-step-name").value.length == 0 ||
          document.getElementById("clientApproveFolder-value").value.length == 0 ||
          rejectValid() == false)
      {
         document.getElementById("simple-workflow-action:ok-button").disabled = true;
      }
      else
      {
         document.getElementById("simple-workflow-action:ok-button").disabled = false;
      }
   }
   
   function rejectValid()
   {
      var result = true;
      
      if (document.forms['simple-workflow-action']['simple-workflow-action:reject-step-present'][0].checked && 
          (document.getElementById("simple-workflow-action:reject-step-name").value.length == 0 ||
           document.getElementById("clientRejectFolder-value").value.length == 0))
      {
         result = false;
      }
      
      return result;
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="simple-workflow-action">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/new_rule_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{WizardManager.title}" /></div>
                              <div class="mainSubText"><h:outputText value="#{WizardManager.description}" /></div>
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
                              
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan="2" class="mainSubTitle"><h:outputText value="#{msg.set_action_values}" /></td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.approve_flow}"/></td>
                                 </tr>
                                 <tr>
                                    <td><nobr><h:outputText value="#{msg.approve_step_name}"/>:</nobr></td>
                                    <td width="90%">
                                       <h:inputText id="approve-step-name" value="#{WizardManager.bean.actionProperties.approveStepName}" 
                                                    onkeyup="javascript:checkButtonState();" />
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr><td colspan="2"><h:outputText value="#{msg.move_or_copy}"/></td></tr>
                                 <tr>
                                    <td colspan="2">
                                       <table cellpadding="2" cellspacing="2" border="0">
                                          <tr>
                                             <td valign="top">
                                                <h:selectOneRadio value="#{WizardManager.bean.actionProperties.approveAction}">
                                                   <f:selectItem itemValue="move" itemLabel="#{msg.move}" />
                                                   <f:selectItem itemValue="copy" itemLabel="#{msg.copy}" />
                                                </h:selectOneRadio>
                                             </td>
                                             <td style="padding-left:6px;"></td>
                                             <td valign="top" style="padding-top:10px;"><h:outputText value="#{msg.to}"/>:</td>
                                             <td style="padding-left:6px;"></td>
                                             <td style="padding-top:6px;">
                                                <r:ajaxFolderSelector id="clientApproveFolder"
                                                             label="#{msg.select_destination_prompt}" 
                                                             value="#{WizardManager.bean.actionProperties.approveFolder}" 
                                                             initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
                                                             styleClass="selector" />
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.reject_flow}"/></td>
                                 </tr>
                                 <tr>
                                    <td colspan="2"><h:outputText value="#{msg.want_reject_step}"/></td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table cellpadding="2" cellspacing="2" border="0">                                 
                                          <tr>
                                          <td valign="top">
                                                <h:selectOneRadio id="reject-step-present" value="#{WizardManager.bean.actionProperties.rejectStepPresent}"
                                                                  onclick="javascript:checkButtonState();" >
                                                   <f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
                                                   <f:selectItem itemValue="no" itemLabel="#{msg.no}" />
                                                </h:selectOneRadio>
                                             </td>
                                          </tr>
                                        </table>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table cellpadding="0" cellspacing="0" border="0">
                                          <tr>
                                             <td style="padding-left:24px;"></td>
                                             <td>
                                                <table cellpadding="2" cellspacing="2" border="0">
                                                   <tr>
                                                      <td>
                                                         <nobr><h:outputText value="#{msg.reject_step_name}"/>:</nobr>
                                                         <h:inputText id="reject-step-name" value="#{WizardManager.bean.actionProperties.rejectStepName}" 
                                                                      onkeyup="javascript:checkButtonState();" />
                                                      </td>
                                                   </tr>
                                                   <tr><td class="paddingRow"></td></tr>
                                                   <tr><td><h:outputText value="#{msg.move_or_copy}"/></td></tr>
                                                   <tr>
                                                      <td>
                                                         <table cellpadding="2" cellspacing="2" border="0">
                                                            <tr>
                                                               <td valign="top">
                                                                  <h:selectOneRadio value="#{WizardManager.bean.actionProperties.rejectAction}">
                                                                     <f:selectItem itemValue="move" itemLabel="#{msg.move}" />
                                                                     <f:selectItem itemValue="copy" itemLabel="#{msg.copy}" />
                                                                  </h:selectOneRadio>
                                                               </td>
                                                               <td style="padding-left:6px;"></td>
                                                               <td valign="top" style="padding-top:10px;"><h:outputText value="#{msg.to}"/>:</td>
                                                               <td style="padding-left:6px;"></td>
                                                               <td style="padding-top:6px;">
                                                                  <r:ajaxFolderSelector id="clientRejectFolder"
                                                                               label="#{msg.select_destination_prompt}" 
                                                                               value="#{WizardManager.bean.actionProperties.rejectFolder}" 
                                                                               initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
                                                                               styleClass="selector" />
                                                               </td>
                                                            </tr>
                                                         </table>
                                                      </td>
                                                   </tr>
                                                </table>
                                             </td>
                                          </tr>
                                       </table>
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
                                       <h:commandButton id="ok-button" value="#{msg.ok}" action="#{WizardManager.bean.addAction}" 
                                                        styleClass="wizardButton" disabled="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="cancel-button" value="#{msg.cancel_button}" action="#{WizardManager.bean.cancelAddAction}" 
                                                        styleClass="wizardButton" />
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
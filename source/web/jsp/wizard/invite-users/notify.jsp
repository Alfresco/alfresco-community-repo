<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_inviteusers_notify">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="notify-users">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2">
      
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
         <td width="100%">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/users_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{InviteSpaceUsersWizard.wizardTitle}" /> '<h:outputText value='#{BrowseBean.actionSpace.name}' />'</div>
                              <div class="mainSubText"><h:outputText value="#{InviteSpaceUsersWizard.wizardDescription}" /></div>
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
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <h:outputText styleClass="mainSubTitle" value="#{msg.steps}"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="2" selectedStyleClass="statusListHighlight"
                                    value="2" disabled="true">
                                 <a:listItem value="1" label="1. #{msg.invite_step_1}" />
                                 <a:listItem value="2" label="2. #{msg.invite_step_2}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                           
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                           
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan=2 class="mainSubTitle"><h:outputText value="#{InviteSpaceUsersWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td colspan=2 class="mainSubText"><h:outputText value="#{InviteSpaceUsersWizard.stepDescription}" /></td>
                                 </tr>
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan=2><h:outputText value="#{msg.send_email}" /></td>
                                 </tr>
                                 <tr>
                                    <td colspan=2>
                                       <h:selectOneRadio value="#{InviteSpaceUsersWizard.notify}">
                                          <f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
                                          <f:selectItem itemValue="no" itemLabel="#{msg.no}" />
                                       </h:selectOneRadio>
                                    </td>
                                 </tr>
                                 
                                 <%-- Enter the message subject and body --%>
                                 <tr><td colspan="2" class="mainSubTitle"><h:outputText value="#{msg.email_message}" /></td></tr>
                                 <tr>
                                    <td style="padding-left:16px"><h:outputText value="#{msg.subject}"/>:</td>
                                    <td>
                                       <h:inputText id="subject" value="#{InviteSpaceUsersWizard.subject}" size="75" maxlength="1024" />&nbsp;*
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td></td>
                                    <td valign="top">
                                       <table cellspacing=0 cellpadding=2 border=0
                                          <tr>
                                             <td><h:outputText value="#{msg.action_mail_template}"/>:</td>
                                             <td>
                                                <%-- Templates drop-down selector --%>
                                                <h:selectOneMenu value="#{InviteSpaceUsersWizard.template}">
                                                   <f:selectItems value="#{TemplateSupportBean.emailTemplates}" />
                                                </h:selectOneMenu>
                                             </td>
                                             <td><h:commandButton value="#{msg.insert_template}" actionListener="#{InviteSpaceUsersWizard.insertTemplate}" styleClass="wizardButton" /></td>
                                             <td><h:commandButton value="#{msg.discard_template}" actionListener="#{InviteSpaceUsersWizard.discardTemplate}" styleClass="wizardButton" disabled="#{InviteSpaceUsersWizard.usingTemplate == null}" /></td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td style="padding-left:16px"><h:outputText value="#{msg.message}"/>:</td>
                                    <td>
                                       <h:inputTextarea value="#{InviteSpaceUsersWizard.body}" 
                                                        rows="4" cols="75" disabled="#{InviteSpaceUsersWizard.usingTemplate != null}" />
                                    </td>
                                 </tr>
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan=2><h:outputText value="#{InviteSpaceUsersWizard.stepInstructions}" /></td>
                                 </tr> 
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.finish_button}" action="#{InviteSpaceUsersWizard.finish}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.back_button}" action="#{InviteSpaceUsersWizard.back}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr><td class="wizardButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel_button}" action="#{InviteSpaceUsersWizard.cancel}" styleClass="wizardButton" />
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
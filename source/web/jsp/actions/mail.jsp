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
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page import="org.alfresco.web.bean.users.MailInviteSpaceUsersWizard" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_action_email">

   
<script type="text/javascript">
   
   /* The mootools API is used since its lib included in head.
      It also usefull to use their String.trim() to disable blank subject field */
   
   addEventToElement(window, 'load', mailPageLoaded, false);

   function mailPageLoaded()
   {
      // ensure the attribute exists
      $("email-action:ok-button").set({disabled:'disabled'});
      $("email-action:subject").focus();
      mailCheckButtonState();
   }
   
   function mailCheckButtonState()
   {
      if ($("email-action:subject").value.trim().length > 0 && $("finishButtonDisabled").value == "false")
      {
         $("email-action:ok-button").disabled = false;
      }
      else
      {
         $("email-action:ok-button").disabled = true;
      }
   }
</script>
   
<f:view>
   
   <h:inputHidden id="finishButtonDisabled" value="#{WizardManager.bean.emailRecipientsDataModel.rowCount le 0}"/>

   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="email-action">
      
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
                                 
                                 <h:panelGroup rendered="#{MailInviteSpaceUsersWizard.haveMaximumUsersBeenReturned}">
                                    <f:verbatim><tr><td colspan="2">
                                    <% out.write("<div style='padding:2px;'></div>");
                                    PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                                    <img src='<%=request.getContextPath()%>/images/icons/info_icon.gif' align='absmiddle' />&nbsp;&nbsp;
                                    </f:verbatim>
                                    <h:outputText id="infoMsg" value="#{MailInviteSpaceUsersWizard.maximumUsersMsg}" />
                                    <f:verbatim>
                                    <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                                    </td></tr></f:verbatim>
                                 </h:panelGroup>
                                 
                                 <%-- Select the email receipients --%>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr><td colspan="2" class="mainSubTitle"><h:outputText value="#{msg.message_recipients}" /></td></tr>
                                 <tr><td colspan="2" style="padding-left:12px">1. <h:outputText value="#{msg.select_recipients}" /></td></tr>
                                 <tr>
                                    <%-- Generic Picker to select Users/Groups --%>
                                    <td colspan=2 style="padding-left:16px">
                                       <a:genericPicker id="picker" filters="#{MailInviteSpaceUsersWizard.filters}"
                                             queryCallback="#{MailInviteSpaceUsersWizard.pickerCallback}"
                                             actionListener="#{WizardManager.bean.addRecipient}" />
                                    </td>
                                 </tr>
                                 
                                 <tr><td colspan="2" style="padding-left:12px">2. <h:outputText value="#{msg.selected_recipients}" /></td></tr>
                                 <tr>
                                    <td colspan=2 style="padding-left:12px">
                                       <h:dataTable value="#{WizardManager.bean.emailRecipientsDataModel}" var="row"
                                                    rowClasses="selectedItemsRow,selectedItemsRowAlt"
                                                    styleClass="selectedItems" headerClass="selectedItemsHeader"
                                                    cellspacing="0" cellpadding="4" 
                                                    rendered="#{WizardManager.bean.emailRecipientsDataModel.rowCount != 0}">
                                          <h:column>
                                             <f:facet name="header">
                                                <h:outputText value="#{msg.name}" />
                                             </f:facet>
                                             <h:outputText value="#{row.name}" />
                                          </h:column>
                                          <h:column>
                                             <a:actionLink actionListener="#{WizardManager.bean.removeRecipient}" image="/images/icons/delete.gif"
                                                           value="#{msg.remove}" showLink="false" style="padding-left:6px" />
                                          </h:column>
                                       </h:dataTable>
                                       <a:panel id="no-items" rendered="#{WizardManager.bean.emailRecipientsDataModel.rowCount == 0}">
                                          <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
                                             <tr>
                                                <td colspan='2' class='selectedItemsHeader'><h:outputText id="no-items-name" value="#{msg.name}" /></td>
                                             </tr>
                                             <tr>
                                                <td class='selectedItemsRow'><h:outputText id="no-items-msg" value="#{msg.no_selected_items}" /></td>
                                             </tr>
                                          </table>
                                       </a:panel>
                                    </td>
                                 </tr>
                                 
                                 <%-- Enter the message subject and body --%>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr><td colspan="2" class="mainSubTitle"><h:outputText value="#{msg.email_message}" /></td></tr>
                                 <tr>
                                    <td style="padding-left:16px"><h:outputText value="#{msg.subject}"/>:</td>
                                    <td width="90%">
                                       <h:inputText id="subject" value="#{WizardManager.bean.actionProperties.subject}" size="75" maxlength="1024" 
                                                    onkeyup="javascript:mailCheckButtonState();" />&nbsp;*
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td></td>
                                    <td valign="top">
                                       <table cellspacing="0" cellpadding="2" border="0">
                                          <tr>
                                             <td><h:outputText value="#{msg.action_mail_template}"/>:</td>
                                             <td>
                                                <%-- Templates drop-down selector --%>
                                                <h:selectOneMenu value="#{WizardManager.bean.actionProperties.template}">
                                                     <f:selectItems value="#{TemplateSupportBean.notifyEmailTemplates}" />
                                                </h:selectOneMenu>
                                             </td>
                                             <td><h:commandButton value="#{msg.insert_template}" actionListener="#{WizardManager.bean.insertTemplate}" styleClass="wizardButton" /></td>
                                             <td><h:commandButton value="#{msg.discard_template}" actionListener="#{WizardManager.bean.discardTemplate}" styleClass="wizardButton" disabled="#{WizardManager.bean.usingTemplate == null}" /></td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td style="padding-left:16px"><h:outputText value="#{msg.message}"/>:</td>
                                    <td>
                                       <h:inputTextarea value="#{WizardManager.bean.actionProperties.message}" 
                                                        rows="4" cols="75" disabled="#{WizardManager.bean.usingTemplate != null}" />
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
                                                        styleClass="wizardButton" />
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

<%
MailInviteSpaceUsersWizard wizard = (MailInviteSpaceUsersWizard)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "MailInviteSpaceUsersWizard");
if (wizard != null) 
{
   wizard.init(new HashMap());
}
%>

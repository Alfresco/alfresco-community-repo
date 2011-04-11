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
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<%
if (Application.getWizardManager().getState() == null)
{
   response.sendRedirect(request.getContextPath() + "/faces/jsp/dashboards/container.jsp");
   return;
}
%>

<r:page title="<%=Application.getWizardManager().getTitle() %>"
        doctypeRootElement="html"
        doctypePublic="-//W3C//DTD HTML 4.01 Transitional//EN"
        doctypeSystem="http://www.w3c.org/TR/html4/loose.dtd">
  <f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>

   <h:form acceptcharset="UTF-8" id="wizard">
   
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
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif); width:4px;"></td>
                  <td style="background-color: #dfe6ed;">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" style="width:100%;">
                        <tr>
                           <td style="width:32px;">
                              <h:graphicImage id="wizard-logo" url="#{WizardManager.icon}" alt=""/>
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{WizardManager.title}" /></div>
                              <div class="mainSubTitle"><h:outputText value="#{WizardManager.subTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{WizardManager.description}" /></div>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif); width: 4px;"></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9" alt=""/></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9" alt=""/></td>
               </tr>
               
               <%-- Details --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif); width:4px;"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" style="width: 100%;">
                        <tr>
                           <td style="width:20%;" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <h:outputText styleClass="mainSubTitle" value="#{msg.steps}"/><br/>
                              <a:modeList itemSpacing="3" iconColumnWidth="2" selectedStyleClass="statusListHighlight"
                                          value="#{WizardManager.currentStepAsString}" disabled="true">
                                <a:listItems value="#{WizardManager.stepItems}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                           
                           <td style="width:100%;" valign="top">
                           
                              <a:errors message="#{WizardManager.errorMessage}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" style="border-width: 0px; width: 100%;">
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{WizardManager.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="#{WizardManager.stepDescription}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td style="width: 100%;" valign="top">
                                       <f:subview id="wizard-body">
                                       <h:outputText id="validation_invalid_character" style="display:none" value="#{msg.validation_invalid_character}" />
                              		 <jsp:include page="<%=Application.getWizardManager().getPage() %>" />
                              	       </f:subview>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><h:outputText value="#{WizardManager.stepInstructions}" /></td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="next-button" styleClass="wizardButton" 
                                                        value="#{WizardManager.nextButtonLabel}" 
                                                        action="#{WizardManager.next}" 
                                                        disabled="#{WizardManager.nextButtonDisabled}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="back-button" styleClass="wizardButton" 
                                                        value="#{WizardManager.backButtonLabel}" 
                                                        action="#{WizardManager.back}" 
                                                        disabled="#{WizardManager.backButtonDisabled}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="finish-button" styleClass="wizardButton"
                                                        value="#{WizardManager.finishButtonLabel}" 
                                                        action="#{WizardManager.finish}" 
                                                        disabled="#{WizardManager.finishButtonDisabled}" />
                                    </td>
                                 </tr>
                                 <tr><td class="wizardButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="cancel-button" styleClass="wizardButton"
                                                        value="#{WizardManager.cancelButtonLabel}" 
                                                        action="#{WizardManager.cancel}" immediate="true" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif); width: 4px;"></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4" alt=""/></td>
                  <td align="center" style="width: 100%; background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4" alt=""/></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
  </f:view>
</r:page>

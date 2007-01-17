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
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page title="<%=Application.getWizardManager().getTitle() %>">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

   <h:form acceptCharset="UTF-8" id="wizard">
   
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
                              <h:graphicImage id="wizard-logo" url="#{WizardManager.icon}" />
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
                           <td width="20%" valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
                              <h:outputText styleClass="mainSubTitle" value="#{msg.steps}"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="2" selectedStyleClass="statusListHighlight"
                                    value="#{WizardManager.currentStepAsString}" disabled="true">
                                 <a:listItems value="#{WizardManager.stepItems}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "lbgrey"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                           
                              <a:errors message="#{WizardManager.errorMessage}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{WizardManager.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="#{WizardManager.stepDescription}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td width="100%" valign="top">
                                       <f:subview id="wizard-body">
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
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
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
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "lbgrey"); %>
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
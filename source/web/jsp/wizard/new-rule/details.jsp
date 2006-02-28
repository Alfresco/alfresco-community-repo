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

<r:page titleId="title_new_rule_details">

<script language="JavaScript1.2">
   function checkButtonState(inputField)
   {
      var disabled = (inputField.value.length == 0);
      document.getElementById("new-rule-details:next-button").disabled = disabled;
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="new-rule-details">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/new_rule_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{NewRuleWizard.wizardTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{NewRuleWizard.wizardDescription}" /></div>
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
                                          value="1" disabled="true">
                                 <a:listItem value="1" label="1. #{msg.details}" />
                                 <a:listItem value="2" label="2. #{msg.conditions}" />
                                 <a:listItem value="3" label="3. #{msg.actions}" />
                                 <a:listItem value="4" label="4. #{msg.summary}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                              
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan="2" class="mainSubTitle"><h:outputText value="#{NewRuleWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.type}"/>:</td>
                                    <td width="90%">
                                       <h:selectOneMenu value="#{NewRuleWizard.type}" disabled="#{NewRuleWizard.inEditMode}">
                                          <f:selectItems value="#{NewRuleWizard.types}" />
                                       </h:selectOneMenu>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.title}"/>:</td>
                                    <td>
                                       <h:inputText id="title" value="#{NewRuleWizard.title}" size="35" maxlength="1024"
                                                    onkeyup="javascript:checkButtonState(this);" />&nbsp;*
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.description}"/>:</td>
                                    <td>
                                       <h:inputText value="#{NewRuleWizard.description}" size="35" maxlength="1024" />
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.other_options}" /></td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table><tr>
                                       <td><h:selectBooleanCheckbox value="#{NewRuleWizard.applyToSubSpaces}"/></td>
                                       <td><h:outputText value="#{msg.apply_to_sub_spaces}"/></td>
                                       </tr></table>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table><tr>
                                       <td><h:selectBooleanCheckbox value="#{NewRuleWizard.runInBackground}"/></td>
                                       <td><h:outputText value="#{msg.run_in_background}"/></td>
                                       </tr></table>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <div id="rule-info" style="padding-left: 26px;">
                                          <h:graphicImage alt="" value="/images/icons/info_icon.gif" style="vertical-align: middle;" />&nbsp;
                                          <h:outputText value="#{msg.rule_background_info}" />
                                       </div>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2"><h:outputText value="#{NewRuleWizard.stepInstructions}" /></td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="next-button" value="#{msg.next_button}" action="#{NewRuleWizard.next}" 
                                                        styleClass="wizardButton" disabled="#{NewRuleWizard.title == null}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.finish_button}" action="#{NewRuleWizard.finish}" styleClass="wizardButton" 
                                                        disabled="true"/>
                                    </td>
                                 </tr>
                                 <tr><td class="wizardButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel_button}" action="#{NewRuleWizard.cancel}" styleClass="wizardButton" />
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
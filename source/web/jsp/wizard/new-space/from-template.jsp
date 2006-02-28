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

<r:page titleId="title_new_space_template">

<script language="JavaScript1.2">
   
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      checkButtonState( document.getElementById("new-space-from-template:templateSpaceId") );
   }
   
   function checkButtonState(inputField)
   {
      if (inputField.selectedIndex == 0)
      {
         document.getElementById("new-space-from-template:next-button").disabled = true;
      }
      else
      {
         document.getElementById("new-space-from-template:next-button").disabled = false;
      }
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="new-space-from-template">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/create_space_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{NewSpaceWizard.wizardTitle}" /></div>
                              <div class="mainSubText"><h:outputText value="#{NewSpaceWizard.wizardDescription}" /></div>
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
                                 <a:listItem value="1" label="1. #{msg.starting_space}" />
                                 <a:listItem value="2" label="2. #{msg.space_options}" />
                                 <a:listItem value="3" label="3. #{msg.space_details}" />
                                 <a:listItem value="4" label="4. #{msg.summary}" />
                              </a:modeList>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                           
                           <td width="100%" valign="top">
                           
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                           
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{NewSpaceWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="#{NewSpaceWizard.stepDescription}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="wizardSectionHeading"><h:outputText value="#{msg.template_space}"/></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.select_template}"/></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:selectOneMenu id="templateSpaceId" value="#{NewSpaceWizard.templateSpaceId}" 
                                                        onchange="javascript:checkButtonState(this);">
                                          <f:selectItems value="#{NewSpaceWizard.templateSpaces}" />
                                       </h:selectOneMenu>
                                    </td>
                                 </tr>
                                 <%-- TBD
                                 <tr><td class="details-separator" /></tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.copy_existing_space}"/></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:selectOneRadio value="#{NewSpaceWizard.copyPolicy}" layout="pageDirection">
                                          <f:selectItem itemValue="structure" itemLabel="#{msg.structure}" />
                                          <f:selectItem itemValue="contents" itemLabel="#{msg.structure_contents}" />
                                       </h:selectOneRadio>
                                    </td>
                                 </tr>
                                 --%>
                                 <tr><td class="details-separator" /></tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.space_copy_note}"/></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><h:outputText value="#{NewSpaceWizard.stepInstructions}" /></td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="next-button" value="#{msg.next_button}" action="#{NewSpaceWizard.next}" 
                                                        styleClass="wizardButton" disabled="#{NewSpaceWizard.templateSpaceId == null}"/>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.back_button}" action="#{NewSpaceWizard.back}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.finish_button}" action="#{NewSpaceWizard.finish}" styleClass="wizardButton"
                                                        disabled="true" />
                                    </td>
                                 </tr>
                                 <tr><td class="button-group-separator"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel_button}" action="#{NewSpaceWizard.cancel}" styleClass="wizardButton" />
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
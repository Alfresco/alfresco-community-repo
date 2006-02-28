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

<r:page titleId="title_inviteusers_invite">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="invite-users">
   
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
                                    value="1" disabled="true">
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
                                    <td class="mainSubTitle"><h:outputText value="#{InviteSpaceUsersWizard.stepTitle}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="#{InviteSpaceUsersWizard.stepDescription}" /></td>
                                 </tr>
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{msg.specify_usersgroups}" /></td>
                                 </tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="1. #{msg.select_usersgroups}" /></td>
                                 </tr>
                                 <tr>
                                    <%-- Picker to select Users/Groups --%>
                                    <td><a:genericPicker id="picker" showAddButton="false" filters="#{InviteSpaceUsersWizard.filters}" queryCallback="#{InviteSpaceUsersWizard.pickerCallback}" /></td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.role}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <div style="padding:4px">
                                          <h:selectOneListbox id="roles" style="width:250px" size="5">
                                             <f:selectItems value="#{InviteSpaceUsersWizard.roles}" />
                                          </h:selectOneListbox>
                                       </div>
                                    </td>
                                 </tr>
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText">
                                       2. <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{InviteSpaceUsersWizard.addSelection}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText"><h:outputText value="#{msg.selected_usersgroups}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:dataTable value="#{InviteSpaceUsersWizard.userRolesDataModel}" var="row" 
                                                    rowClasses="selectedItemsRow,selectedItemsRowAlt"
                                                    styleClass="selectedItems" headerClass="selectedItemsHeader"
                                                    cellspacing="0" cellpadding="4" 
                                                    rendered="#{InviteSpaceUsersWizard.userRolesDataModel.rowCount != 0}">
                                          <h:column>
                                             <f:facet name="header">
                                                <h:outputText value="#{msg.name}" />
                                             </f:facet>
                                             <h:outputText value="#{row.label}" />
                                          </h:column>
                                          <h:column>
                                             <a:actionLink actionListener="#{InviteSpaceUsersWizard.removeSelection}" image="/images/icons/delete.gif"
                                                           value="#{msg.remove}" showLink="false" style="padding-left:6px" />
                                          </h:column>
                                       </h:dataTable>
                                       
                                       <a:panel id="no-items" rendered="#{InviteSpaceUsersWizard.userRolesDataModel.rowCount == 0}">
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
                                 
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td><h:outputText value="#{InviteSpaceUsersWizard.stepInstructions}" /></td>
                                 </tr> 
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.next_button}" action="#{InviteSpaceUsersWizard.next}" styleClass="wizardButton" />
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
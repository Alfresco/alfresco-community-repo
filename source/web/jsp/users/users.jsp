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

<r:page titleId="title_users">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="users">
   
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
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr valign="top">
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/users_large.gif" />
                           </td>
                           <td>
                              <div class="mainSubTitle"><h:outputText value='#{NavigationBean.nodeProperties.name}' /></div>
                              <div class="mainTitle"><h:outputText value="#{msg.manage_users}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.manageusers_description}" /></div>
                           </td>
                           <td bgcolor="#465F7D" width=1></td>
                           <td width=100 style="padding-left:2px">
                              <%-- Current object actions --%>
                              <h:outputText style="padding-left:20px;" styleClass="mainSubTitle" value="#{msg.actions}" /><br/>
                              <a:actionLink value="#{msg.create_user}" image="/images/icons/create_user.gif" padding="4" action="createUser" actionListener="#{NewUserWizard.startWizard}" />
                           </td>
                           <td bgcolor="#465F7D" width=1></td>
                           <td width=110 style="padding-left:2px">
                              <%-- View mode settings --%>
                              <h:outputText style="padding-left:26px" styleClass="mainSubTitle" value="#{msg.view}"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif" value="0">
                                 <a:listItem value="0" label="#{msg.user_details}" />
                              </a:modeList>
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
                     <table cellspacing="2" cellpadding="2" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              
                              <%-- Users List --%>
                              <a:panel id="users-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{msg.users}">
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                              <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                 <tr>
                                    <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                    <td class="mainSubText">
                                       <h:outputText value="#{msg.user_search_info}" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                              
                              <div style="padding: 6px;" />
                              <h:inputText value="#{UsersBean.searchCriteria}" size="35" maxlength="1024" />&nbsp;
                              <h:commandButton value="Search" action="#{UsersBean.search}" />&nbsp;
                              <h:commandButton value="Show All" action="#{UsersBean.showAll}" />
                              <div style="padding: 6px;" />
                              
                              <a:richList id="users-list" binding="#{UsersBean.usersRichList}" viewMode="details" pageSize="10"
                                    styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                                    value="#{UsersBean.users}" var="r" initialSortColumn="userName" initialSortDescending="true">
                                 
                                 <%-- Primary column with full name --%>
                                 <a:column primary="true" width="200" style="padding:2px;text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.name}" value="fullName" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <f:facet name="small-icon">
                                       <h:graphicImage url="/images/icons/person.gif" />
                                    </f:facet>
                                    <h:outputText value="#{r.fullName}" />
                                 </a:column>
                                 
                                 <%-- Username column --%>
                                 <a:column width="120" style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.username}" value="userName" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.userName}" />
                                 </a:column>
                                 
                                 <%-- Home Space Path column --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.homespace}"/>
                                    </f:facet>
                                    <r:nodePath value="#{r.homeSpace}" disabled="true" showLeaf="true" />
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:actionLink value="#{msg.modify}" image="/images/icons/edituser.gif" showLink="false" action="editUser" actionListener="#{NewUserWizard.startWizardForEdit}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:actionLink value="#{msg.change_password}" image="/images/icons/change_password.gif" showLink="false" action="changePassword" actionListener="#{UsersBean.setupUserAction}">
                                       <f:param name="id" value="#{r.id}" />
                                    </a:actionLink>
                                    <a:booleanEvaluator value="#{r.userName != 'admin'}">
                                       <a:actionLink value="#{msg.delete}" image="/images/icons/delete_person.gif" showLink="false" action="deleteUser" actionListener="#{UsersBean.setupUserAction}">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </a:booleanEvaluator>
                                 </a:column>
                                 
                                 <a:dataPager styleClass="pager" />
                              </a:richList>
                              
                              </a:panel>
                              
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="0" cellspacing="0" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="adminConsole" styleClass="wizardButton" />
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
               
               <%-- Error Messages --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
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
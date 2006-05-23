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

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<r:page titleId="title_forum">

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="browse-topics">
   
   <%-- Main outer table --%>
   <table cellspacing=0 cellpadding=2>

      <%-- Title bar --%>
      <tr>
         <td colspan=2>
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign=top>
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width=100%>
            <table cellspacing=0 cellpadding=0 width=100%>
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width=4></td>
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width=100%>
                        <tr>
 
                           <%-- actions for topics --%>
                           <a:panel id="forum-actions">
                              <td width=32>
                                 <h:graphicImage id="space-logo" url="/images/icons/forum.gif" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" /></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.forum_info}" id="msg3" /></div>
                              </td>
                              
                              <td align=right>
                                 <%-- Create actions menu --%>
                                 <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="actions_create" value="forum_create_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                              
                              <td style="padding-left:4px" width=80>
                                 <%-- More actions menu --%>
                                 <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                    <r:actions id="actions_more" value="forum_actions_menu" context="#{NavigationBean.currentNode}" />
                                 </a:menu>
                              </td>
                           </a:panel>
                           
                           <td class="separator" width=1></td>
                           <td width=110 valign=middle>
                              <%-- View mode settings --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{ForumsBean.forumViewMode}" actionListener="#{ForumsBean.forumViewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu">
                                 <a:listItem value="details" label="#{msg.details_view}" />
                              </a:modeList>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width=4 height=9></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width=4 height=9></td>
               </tr>
               
               <%-- Details - Topics --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <div style="padding:4px">
                     
                     <a:panel id="topics-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{msg.browse_topics}">

                     <%-- Topics List --%>
                     <a:richList id="topicsList" binding="#{ForumsBean.forumRichList}" viewMode="#{ForumsBean.forumViewMode}" pageSize="#{ForumsBean.forumPageSize}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{ForumsBean.topics}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <h:outputFormat value="#{msg.no_topics}" escape="false" />
                        </f:facet>
                        
                        <%-- Primary column for details view mode --%>
                        <a:column primary="true" width="200" style="padding:2px;text-align:left" rendered="#{ForumsBean.forumViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.topic}" value="name" mode="case-insensitive" styleClass="header"/>
                           </f:facet>
                           <f:facet name="small-icon">
                              <a:actionLink value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </f:facet>
                           <a:actionLink value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
                              <f:param name="id" value="#{r.id}" />
                           </a:actionLink>
                        </a:column>
                        
                        <%-- Replies column for all view modes --%>
                        <a:column style="text-align:left">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.replies}" value="replies" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.replies}" />
                        </a:column>
                        
                        <%-- Actions column --%>
                        <a:column id="col1" actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.actions}"/>
                           </f:facet>
                           
                           <%-- actions are configured in web-client-config-forum-actions.xml --%>
                           <r:actions id="actions" value="forum_actions" context="#{r}" showLink="false" styleClass="inlineAction" />
                        </a:column>
                        
                        <a:dataPager styleClass="pager" />
                     </a:richList>
                        
                     </a:panel>
                     
                     </div>
                                          
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width=4 height=4></td>
                  <td width=100% align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>

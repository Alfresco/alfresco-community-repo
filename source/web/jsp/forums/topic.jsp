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

<r:page titleId="title_topic">

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="browse-posts">
   
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
                        <tr valign=top>
 
                           <%-- actions for forums --%>
                           <a:panel id="topic-actions">
                              <td width=32>
                                 <h:graphicImage id="space-logo" url="/images/icons/topic_large.gif" width="32" height="32" />
                              </td>
                              <td>
                                 <%-- Summary --%>
                                 <div class="mainSubTitle"><h:outputText value="#{msg.topic}" id="msg1" /></div>
                                 <div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" /></div>
                                 <div class="mainSubText"><h:outputText value="#{msg.topic_info}" id="msg3" /></div>
                              </td>
                              <td bgcolor="#465F7D" width=1></td>
                              <td width=110 style="padding-left:2px">
                                 <%-- Current object actions --%>
                                 <h:outputText style="padding-left:20px" styleClass="mainSubTitle" value="#{msg.actions}" id="msg5" /><br>
                                 <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren" id="eval1">
                                    <a:actionLink value="#{msg.post_to_topic}" image="/images/icons/create_post.gif" padding="4" action="createPost" actionListener="#{CreatePostDialog.startWizard}" id="link1" />
                                 </r:permissionEvaluator>
                                 <a:actionLink value="#{msg.view_details}" image="/images/icons/View_details.gif" padding="4" action="showTopicDetails" actionListener="#{BrowseBean.setupSpaceAction}" id="link2">
                                    <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param1" />
                                 </a:actionLink>
                                 <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="Delete" id="eval2">
                                    <a:actionLink value="#{msg.delete_topic}" image="/images/icons/delete.gif" padding="4" action="deleteTopic" actionListener="#{BrowseBean.setupDeleteAction}" id="link3">
                                       <f:param name="id" value="#{NavigationBean.currentNodeId}" id="param1" />
                                    </a:actionLink>
                                 </r:permissionEvaluator>
                              </td>
                           </a:panel>
                                                      
                           <td bgcolor="#465F7D" width=1></td>
                           <td width=110>
                              <%-- View mode settings --%>
                              <h:outputText style="padding-left:26px" styleClass="mainSubTitle" value="#{msg.view}"/><br>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight" selectedImage="/images/icons/Details.gif"
                                    value="#{ForumsBean.topicViewMode}" actionListener="#{ForumsBean.topicViewModeChanged}">
                                 <a:listItem value="details" label="#{msg.details_view}" />
                                 <a:listItem value="bubble" label="#{msg.bubble_view}" />
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
               
               <%-- Details - Posts --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <div style="padding:4px">
                     
                     <a:panel id="posts-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{msg.browse_posts}">

                     <%-- Posts List --%>
                     <a:richList id="postsList" binding="#{ForumsBean.topicRichList}" viewMode="#{ForumsBean.topicViewMode}" pageSize="#{ForumsBean.topicPageSize}"
                           styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                           value="#{ForumsBean.posts}" var="r">
                        
                        <%-- component to display if the list is empty --%>
                        <f:facet name="empty">
                           <h:outputFormat value="#{msg.no_posts}" escape="false" />
                        </f:facet>
                        
                        <%-- Content column for all view modes --%>
                        <a:column primary="true" width="200" style="padding:2px;text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.post}" />
                           </f:facet>
                           <h:outputText value="#{r.message}" />
                        </a:column>
                        
                        <%-- Author column for the details view mode --%>
                        <a:column style="text-align:left" rendered="#{ForumsBean.topicViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.author}" value="creator" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.creator}" />
                        </a:column>
                        
                        <%-- Posted time column for details view mode --%>
                        <a:column style="text-align:left; white-space:nowrap" 
                                  rendered="#{ForumsBean.topicViewMode == 'details'}">
                           <f:facet name="header">
                              <a:sortLink label="#{msg.posted}" value="created" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- topic name column for bubble view mode --%>
                        <a:column style="text-align:left;" 
                                  rendered="#{ForumsBean.topicViewMode == 'bubble'}">
                           <f:facet name="header">
                              <h:outputText value="#{msg.post}:" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{NavigationBean.nodeProperties.name}" />
                        </a:column>
                        
                        <%-- reply to column for bubble view mode --%>
                        <a:column style="text-align:left;" 
                                  rendered="#{ForumsBean.topicViewMode == 'bubble' && r.replyTo != null}">
                           <f:facet name="header">
                              <h:outputText value="#{msg.reply_to}:" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.replyTo}" />
                        </a:column>
                        
                        <%-- Posted time column for bubble view mode --%>
                        <a:column style="text-align:left; white-space:nowrap" 
                                  rendered="#{ForumsBean.topicViewMode == 'bubble'}">
                           <f:facet name="header">
                              <h:outputText value="#{msg.on}:" styleClass="header"/>
                           </f:facet>
                           <h:outputText value="#{r.created}">
                              <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                           </h:outputText>
                        </a:column>
                        
                        <%-- Actions column --%>
                        <a:column actions="true" style="text-align:left">
                           <f:facet name="header">
                              <h:outputText value="#{msg.actions}"/>
                           </f:facet>
                           <r:permissionEvaluator value="#{NavigationBean.currentNode}" allow="CreateChildren">
                              <a:actionLink value="#{msg.post_reply}" image="/images/icons/post_reply.gif" showLink="false" styleClass="inlineAction" action="createReply" actionListener="#{CreateReplyDialog.startWizard}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </r:permissionEvaluator>
                           <r:permissionEvaluator value="#{r}" allow="Write">
                              <a:actionLink value="#{msg.edit_post}" image="/images/icons/edit_icon.gif" showLink="false" styleClass="inlineAction" action="editPost" actionListener="#{BrowseBean.setupContentAction}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </r:permissionEvaluator>
                           <r:permissionEvaluator value="#{r}" allow="Delete">
                              <a:actionLink value="#{msg.delete_post}" image="/images/icons/delete.gif" showLink="false" styleClass="inlineAction" action="deletePost" actionListener="#{BrowseBean.setupDeleteAction}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </r:permissionEvaluator>
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

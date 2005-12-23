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
<%@ page import="org.alfresco.web.bean.ForumsBean.TopicBubbleViewRenderer" %>

<r:page titleId="title_create_reply">

<script language="JavaScript1.2">
   function checkButtonState()
   {
      if (document.getElementById("create-reply:message").value.length == 0)
      {
         document.getElementById("create-reply:ok-button").disabled = true;
      }
      else
      {
         document.getElementById("create-reply:ok-button").disabled = false;
      }
   }

</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="create-reply">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/post_reply_large.gif" />
                           </td>
                           <td>
                              <div class="mainSubTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" /></div>
                              <div class="mainTitle"><h:outputText value="#{msg.post_reply}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.create_reply_description}" /></div>
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
                              
                              <a:errors message="#{msg.error_create_reply_dialog}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.reply_message}" /></td>
                                 </tr>
                                 <tr>
                                    <td valign="top"><h:outputText value="#{msg.message}" />:</td>
                                    <td>
                                       <h:inputTextarea id="message" value="#{CreateReplyDialog.content}" rows="6" cols="70" 
                                                        onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();" />
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2"><h:outputText value="#{msg.create_reply_finish}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2">
                                       <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                          <tr>
                                          <td width="100%">
                                             <% TopicBubbleViewRenderer.renderBubbleTop(out, request.getContextPath(), "yellow", "#FFF5A3"); %>
                                             <h:outputText value="#{msg.posted}:" styleClass="mainSubTitle" />&nbsp;
                                             <h:outputText value="#{BrowseBean.document.properties.created}">
                                                <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                                             </h:outputText>
                                             <% TopicBubbleViewRenderer.renderBubbleMiddle(out, request.getContextPath(), "yellow"); %>
                                             <h:outputText value="#{CreateReplyDialog.replyContent}" escape="false" />
                                             <% TopicBubbleViewRenderer.renderBubbleBottom(out, request.getContextPath(), "yellow"); %>
                                          </td>
                                          <td valign="top">
                                             <h:graphicImage id="poster" url="/images/icons/user_large.gif" /><br/>
                                             <h:outputText value="#{BrowseBean.document.properties.creator}" />
                                          </td>
                                          </tr>
                                       </table>
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.reply}" action="#{CreateReplyDialog.finish}" 
                                                        styleClass="wizardButton" disabled="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="#{CreateReplyDialog.cancel}" styleClass="wizardButton" />
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
    
   <script language="JavaScript1.2">
      document.getElementById("create-reply:message").focus();
   </script>

</f:view>

</r:page>
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

<r:page titleId="title_save_search">

<script language="JavaScript1.2">

   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("save-search:name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("save-search:name").value.length == 0 )
      {
         document.getElementById("save-search:ok-button").disabled = true;
      }
      else
      {
         document.getElementById("save-search:ok-button").disabled = false;
      }
   }

</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="save-search">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/save_search_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.save_edit_search}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.save_search_description}" /></div>
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
                                 <a:panel id="edit-panel" rendered="#{AdvancedSearchBean.editSearchName != null}">
                                 <tr>
                                    <td width="100%" valign="top" colspan="2" style="padding-bottom:6px">
                                       <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                                       <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                          <tr>
                                             <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                             <td class="mainSubText">
                                                <h:outputFormat value="#{msg.saved_search_warning}">
                                                   <f:param value="#{AdvancedSearchBean.editSearchName}" />
                                                </h:outputFormat>
                                             </td>
                                          </tr>
                                       </table>
                                       <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                                    </td>
                                 </tr>
                                 </a:panel>
                                 
                                 <tr>
                                    <td colspan="2" class="wizardSectionHeading"><h:outputText value="#{msg.search_props}" /></td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.name}" />:</td>
                                    <td>
                                       <h:inputText id="name" value="#{AdvancedSearchBean.searchName}" size="35" maxlength="1024" 
                                                    onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();"/>&nbsp;*
                                    </td>
                                 </tr>
                                 <tr>
                                    <td><h:outputText value="#{msg.description}" />:</td>
                                    <td>
                                       <h:inputText value="#{AdvancedSearchBean.searchDescription}" size="35" maxlength="1024" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.save}" action="#{AdvancedSearchBean.saveEditSearchOK}" 
                                                        styleClass="wizardButton" disabled="true" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="browse" styleClass="wizardButton" />
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
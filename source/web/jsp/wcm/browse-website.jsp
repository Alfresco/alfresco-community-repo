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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/wcm.tld" prefix="w" %>

<%@ page buffer="64kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_browse_website">

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="website">
   
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
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing=4 cellpadding=0 width=100%>
                        <tr>
                           <td width=32>
                              <h:graphicImage id="space-logo" url="/images/icons/website_large.gif" width="32" height="32" />
                           </td>
                           <td>
                              <%-- Summary --%>
                              <div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.website_info}" id="msg3" /></div>
                              <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                           </td>
                           <td style="white-space:nowrap" width=150>
                              <nobr>
                              <%-- More actions menu --%>
                              <a:menu id="actions-menu" itemSpacing="4" label="#{msg.actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <r:actions id="acts-website" value="browse_website_menu" context="#{AVMBrowseBean.website}" />
                              </a:menu>
                              </nobr>
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
               
               <%-- Error Messages --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td>
                     <h:message for="sandboxes-panel" styleClass="statusMessage" />
                     <%-- messages tag to show messages not handled by other specific message tags --%> 
                     <a:errors message="" infoClass="statusWarningText" errorClass="statusErrorText" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Staging sandbox --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     <%-- Current Webapp selection - only displayed if >1 webapps are present --%>
                     <h:panelGroup rendered="#{AVMBrowseBean.webappsSize > 1}">
                        <h:outputText value="#{msg.webapp_current}:&nbsp;" styleClass="mainSubTitle" escape="false" />
                        <h:selectOneMenu value="#{AVMBrowseBean.webapp}" onchange="document.forms['website'].submit(); return true;">
                           <f:selectItems value="#{AVMBrowseBean.webapps}" />
                        </h:selectOneMenu>
                        <f:verbatim><div style="padding:4px"></div></f:verbatim>
                     </h:panelGroup>
                     
                     <a:panel id="staging-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.staging_sandbox}">
                        
                        <%-- Staging Sandbox Info --%>
                        <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
                        <table cellspacing=2 cellpadding=2 border=0 width=100%>
                           <tr>
                              <td align=left width=32><a:actionLink image="/images/icons/sandbox_large.gif" showLink="false" value="#{msg.staging_sandbox}" actionListener="#{AVMBrowseBean.setupSandboxAction}" action="browseSandbox" /></td>
                              <td align=left><h:outputText value="#{msg.staging_sandbox}" styleClass="mainSubTitle" /></td>
                              <td align=right>
                                 <a:actionLink id="actBrowse" value="#{msg.sandbox_browse}" image="/images/icons/space_small.gif" actionListener="#{AVMBrowseBean.setupSandboxAction}" action="browseSandbox" />
                                 &nbsp;&nbsp;
                                 <a:actionLink id="actPreview" value="#{msg.sandbox_preview}" image="/images/icons/preview_website.gif" href="#{AVMBrowseBean.stagingPreviewUrl}" target="new" />
                                 <%-- Disabled action for GA
                                 <a:actionLink id="actSnap" value="#{msg.sandbox_snapshot}" image="/images/icons/create_snapshot.gif" actionListener="#{AVMBrowseBean.setupSandboxAction}" action="dialog:snapshotSandbox" />
                                 --%>
                              </td>
                           </tr>
                           <tr>
                              <td></td>
                              <td colspan=2>
                                 <div style='line-height:6px'>
                                    <h:outputText value="#{AVMBrowseBean.stagingSummary}" escape="false" />
                                 </div>
                              </td>
                           </tr>
                           <tr>
                              <td></td>
                              <td colspan=2>
                                 <a:panel id="snapshots-panel" rendered="#{AVMBrowseBean.isManagerRole}" label="#{msg.recent_snapshots}"
                                       progressive="true" expanded="false" styleClass="mainSubTitle">
                                 <div style='padding-left:16px;padding-top:8px;padding-bottom:4px'>
                                    <%-- Sandbox snapshots list --%>
                                    <table cellspacing=2 cellpadding=0 width=100% class="snapshotItemsList">
                                       <tr>
                                          <td><img src="<%=request.getContextPath()%>/images/icons/filter.gif" width=16 height=16></td>
                                          <td style="padding-left:8px;width:120px"><nobr><h:outputText id="msg-date" value="#{msg.date_filter_when}" />:</nobr></td>
                                          <td width=100%>
                                             <a:modeList id="snap-filter" itemSpacing="2" iconColumnWidth="0" horizontal="true" selectedLinkStyle="font-weight:bold"
                                                   value="#{AVMBrowseBean.snapshotDateFilter}" actionListener="#{AVMBrowseBean.snapshotDateFilterChanged}">
                                                <a:listItem id="f1" value="all" label="#{msg.date_filter_all}" />
                                                <a:listItem id="f2" value="today" label="#{msg.date_filter_today}" />
                                                <a:listItem id="f3" value="week" label="#{msg.date_filter_week}" />
                                                <a:listItem id="f4" value="month" label="#{msg.date_filter_month}" />
                                             </a:modeList>
                                          </td>
                                       </tr>
                                    </table>
                                    <div style='padding:2px'></div>
                                    <w:sandboxSnapshots id="snapshots" value="#{AVMBrowseBean.stagingStore}" dateFilter="#{AVMBrowseBean.snapshotDateFilter}" />
                                 </div>
                                 </a:panel>
                              </td>
                           </tr>
                        </table>
                        <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "lbgrey"); %>
                        
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - User sandboxes --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <a:panel id="sandboxes-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.user_sandboxes}">
                        
                        <%-- User Sandboxes List --%>
                        <w:userSandboxes id="sandboxes" binding="#{AVMBrowseBean.userSandboxes}" value="#{AVMBrowseBean.website.nodeRef}" webapp="#{AVMBrowseBean.webapp}" />
                        
                     </a:panel>
                     
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

<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
   
   <h:form acceptcharset="UTF-8" id="website">
   
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
                        <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "innerwhite", "white"); %>
                        <table cellspacing=2 cellpadding=2 border=0 width=100%>
                           <tr>
                              <td align=left width=32>
                              	<a:actionLink image="/images/icons/sandbox_large.gif" showLink="false" value="#{msg.staging_sandbox}" 
                              	              actionListener="#{AVMBrowseBean.setupSandboxAction}" action="browseSandbox" />
                              </td>
                              <td align=left><h:outputText value="#{msg.staging_sandbox}" styleClass="mainSubTitle" /></td>
                              <td align=right>
                                 <a:actionLink id="actLinks" value="#{msg.check_links}" image="/images/icons/run_link_validation.gif" 
                                               actionListener="#{DialogManager.setupParameters}" action="dialog:linkValidation">
                                    <f:param name="store" value="#{AVMBrowseBean.stagingStore}" />
                                    <f:param name="webapp" value="#{AVMBrowseBean.webapp}" />
                                    <f:param name="mode" value="runReport" />
                                 </a:actionLink>
                                 &nbsp;
                                 <a:actionLink id="actBrowse" value="#{msg.sandbox_browse}" image="/images/icons/space_small.gif" 
                                               actionListener="#{AVMBrowseBean.setupSandboxAction}" action="browseSandbox" />
                                 &nbsp;
                                 <a:actionLink id="actPreview" value="#{msg.sandbox_preview}" image="/images/icons/preview_website.gif" 
                                               href="#{AVMBrowseBean.stagingPreviewUrl}" target="new" />
                                 &nbsp;
                                 <a:actionLink id="actRefresh" rendered="#{AVMBrowseBean.isManagerRole}" value="#{msg.sandbox_refresh}" 
                                               actionListener="#{AVMBrowseBean.refreshSandbox}" image="/images/icons/reset.gif" />
                                 &nbsp;
                                 <a:actionLink id="actViewDeployReport" rendered="#{AVMBrowseBean.hasDeployBeenAttempted}" value="#{msg.deployment_report_action}" 
                                               actionListener="#{AVMBrowseBean.setupSandboxAction}" action="dialog:viewDeploymentReport"
                                               image="/images/icons/deployment_report.gif"  />
                                 
                                 <%-- Disabled action for GA
                                 <a:actionLink id="actSnap" value="#{msg.sandbox_snapshot}" image="/images/icons/create_snapshot.gif" 
                                               actionListener="#{AVMBrowseBean.setupSandboxAction}" action="dialog:snapshotSandbox" />
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
                           <tr>
                              <td></td>
                              <td colspan=2>
                                 <a:panel id="pending-submission-panel" rendered="#{AVMBrowseBean.isManagerRole}" label="#{msg.pending_submissions}"
                                       progressive="true" expanded="false" styleClass="mainSubTitle">
                                    <div style='padding-left:16px;padding-top:8px;padding-bottom:4px'>
                                       <%-- Pending submission list --%>
                                       <w:pendingSubmissions id="pending-submissions" value="#{AVMBrowseBean.stagingStore}" />
                                    </div>
                                 </a:panel>
                              </td>
                           </tr>
                        </table>
                        <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "innerwhite"); %>
                        
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

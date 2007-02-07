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

<r:page titleId="title_browse_sandbox">

<f:view>
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="browse-sandbox">
   
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
                              <h:graphicImage id="space-logo" url="#{AVMBrowseBean.icon}" width="32" height="32" />
                           </td>
                           <td>
                              <%-- Summary --%>
                              <div class="mainTitle"><h:outputText value="#{AVMBrowseBean.sandboxTitle}" id="msg2" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.sandbox_info}" id="msg3" /></div>
                              <div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
                           </td>
                           <td style="white-space:nowrap">
                              <a:actionLink value="#{msg.sandbox_preview}" image="/images/icons/preview_website.gif" href="#{AVMBrowseBean.sandboxPreviewUrl}" target="new" />
                           </td>
                           <r:permissionEvaluator value="#{AVMBrowseBean.currentPathNode}" allow="CreateChildren" id="eval1">
                           <td style="padding-left:4px;white-space:nowrap" width=120>
                              <%-- Create actions menu --%>
                              <a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <r:actions id="acts_create" value="avm_create_menu" context="#{AVMBrowseBean.currentPathNode}" />
                              </a:menu>
                           </td>
                           </r:permissionEvaluator>
                           <%-- More actions menu --%>
                           <%-- <td style="padding-left:4px" width=80>
                              <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                 <r:actions id="acts_more" value="avm_more_menu" context="#{AVMBrowseBean.currentPathNode}" />
                              </a:menu>
                           </td>--%>
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
               
               <%-- Website Path Breadcrumb --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding-left:8px;padding-top:4px;padding-bottom:4px">
                     <a:breadcrumb value="#{AVMBrowseBean.location}" styleClass="title" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Folders --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <a:panel id="folders-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              label="#{msg.website_browse_folders}">
                        
                        <a:richList id="folder-list" binding="#{AVMBrowseBean.foldersRichList}" viewMode="details" pageSize="10"
                              styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                              value="#{AVMBrowseBean.folders}" var="r">
                           
                           <%-- Primary column with folder name --%>
                           <a:column primary="true" width="200" style="padding:2px;text-align:left">
                              <f:facet name="header">
                                 <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="col1-act1" value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{AVMBrowseBean.clickFolder}" showLink="false">
                                    <f:param name="id" value="#{r.id}" />
                                 </a:actionLink>
                              </f:facet>
                              <a:actionLink id="col1-act2" value="#{r.name}" actionListener="#{AVMBrowseBean.clickFolder}">
                                 <f:param name="id" value="#{r.id}" />
                              </a:actionLink>
                           </a:column>
                           
                           <%-- Description column
                           <a:column id="col4" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col4-sort" label="#{msg.description}" value="description" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col4-txt" value="#{r.description}" />
                           </a:column>--%>
                           
                           <%-- Creator column --%>
                           <a:column id="col5" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col5-sort" label="#{msg.creator}" value="creator" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col5-txt" value="#{r.creator}" />
                           </a:column>
                           
                           <%-- Created Date column --%>
                           <a:column id="col6" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col6-sort" label="#{msg.created_date}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col6-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modifier column --%>
                           <a:column id="col6_1" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col6_1-sort" label="#{msg.modifier}" value="modifier" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col6_1-txt" value="#{r.modifier}" />
                           </a:column>
                           
                           <%-- Modified Date column --%>
                           <a:column id="col7" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col7-sort" label="#{msg.modified_date}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col7-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Folder Actions column --%>
                           <a:column id="col9" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="col9-txt" value="#{msg.actions}"/>
                              </f:facet>
                              
                              <%-- actions are configured in web-client-config-wcm-actions.xml --%>
                              <r:actions id="col9-acts1" value="avm_folder_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           </a:column>
                           
                           <a:dataPager id="pager1" styleClass="pager" />
                        </a:richList>
                        
                     </a:panel>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
               </tr>
               
               <%-- Details - Files --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
                  <td style="padding:4px">
                     
                     <a:panel id="files-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle"
                              label="#{msg.website_browse_files}">
                        
                        <a:richList id="files-list" binding="#{AVMBrowseBean.filesRichList}" viewMode="details" pageSize="10"
                              styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
                              value="#{AVMBrowseBean.files}" var="r">
                           
                           <%-- Primary column for details view mode --%>
                           <a:column id="col10" primary="true" width="200" style="padding:2px;text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col10-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
                              </f:facet>
                              <f:facet name="small-icon">
                                 <a:actionLink id="col10-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
                              </f:facet>
                              <a:actionLink id="col10-act2" value="#{r.name}" href="#{r.url}" target="new" />
                           </a:column>
                           
                           <%-- Description column
                           <a:column id="col13" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col13-sort" label="#{msg.description}" value="description" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col13-txt" value="#{r.description}" />
                           </a:column> --%>
                           
                           <%-- Size column --%>
                           <a:column id="col15" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col15-sort" label="#{msg.size}" value="size" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col15-txt" value="#{r.size}">
                                 <a:convertSize />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Creator column --%>
                           <a:column id="col15a" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col15a-sort" label="#{msg.creator}" value="creator" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col15a-txt" value="#{r.creator}" />
                           </a:column>
                           
                           <%-- Created Date column --%>
                           <a:column id="col16" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col16-sort" label="#{msg.created_date}" value="created" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col16-txt" value="#{r.created}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Modifier column --%>
                           <a:column id="col13" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col13-sort" label="#{msg.modifier}" value="modifier" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col13-txt" value="#{r.modifier}" />
                           </a:column>
                           
                           <%-- Modified Date column --%>
                           <a:column id="col17" style="text-align:left">
                              <f:facet name="header">
                                 <a:sortLink id="col17-sort" label="#{msg.modified_date}" value="modified" styleClass="header"/>
                              </f:facet>
                              <h:outputText id="col17-txt" value="#{r.modified}">
                                 <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                              </h:outputText>
                           </a:column>
                           
                           <%-- Content Actions column --%>
                           <a:column id="col18" actions="true" style="text-align:left">
                              <f:facet name="header">
                                 <h:outputText id="col18-txt" value="#{msg.actions}"/>
                              </f:facet>
                              
                              <%-- actions are configured in web-client-config-wcm-actions.xml --%>
                              <r:actions id="col18-acts1" value="avm_file_browse" context="#{r}" showLink="false" styleClass="inlineAction" />
                           </a:column>
                           
                           <a:dataPager id="pager2" styleClass="pager" />
                           
                        </a:richList>
                        
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

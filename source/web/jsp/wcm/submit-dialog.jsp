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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:comment").focus();
   }
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.submit_submission_info}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px">
   <h:outputText value="#{msg.submit_comment}"/>
   <h:inputText id="comment" value="#{DialogManager.bean.comment}" maxlength="1024" size="35" />
   <h:outputText value="#{msg.submit_snapshotlabel}"/>
   <h:inputText id="label" value="#{DialogManager.bean.label}" maxlength="1024" size="35" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.workflow}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px">
   <h:outputText value="#{msg.submit_workflow_selection}"/>
   <a:selectList id="workflow-list" multiSelect="false" styleClass="selectListTable" itemStyleClass="selectListItem"
         value="#{DialogManager.bean.workflowSelectedValue}">
      <a:listItems value="#{DialogManager.bean.workflowList}" />
   </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.modified_items}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%" style="margin-left:8px">
   <h:panelGroup rendered="#{DialogManager.bean.warningItemsSize != 0}">
      <f:verbatim><% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %></f:verbatim>
      <h:panelGrid columns="2" cellpadding="0" cellpadding="0">
         <h:graphicImage url="/images/icons/warning.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
         <h:outputText styleClass="mainSubText" value="#{msg.submit_not_submit_warning}" />
      </h:panelGrid>
      <f:verbatim><% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %></f:verbatim>
      
      <a:richList id="warning-list" viewMode="details" value="#{DialogManager.bean.warningItems}" 
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
                  altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="name">
         
         <%-- Primary column for details view mode --%>
         <a:column id="col1" primary="true" width="100" style="padding:2px;text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <f:facet name="small-icon">
               <a:actionLink id="col1-icon" value="#{r.name}" href="#{r.url}" target="new" image="#{r.icon}" showLink="false" styleClass="inlineAction" />
            </f:facet>
            <a:actionLink id="col1-name" value="#{r.name}" href="#{r.url}" target="new" />
         </a:column>
         
         <%-- Description columns --%>
         <a:column id="col2" width="200" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
            </f:facet>
            <h:outputText id="col2-desc" value="#{r.description}" />
         </a:column>
         
         <%-- Description columns --%>
         <a:column id="col3" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.path}" value="path" styleClass="header"/>
            </f:facet>
            <h:outputText id="col3-path" value="#{r.path}" />
         </a:column>
         
         <%-- Modified Date column --%>
         <a:column id="col4" style="text-align:left; white-space:nowrap">
            <f:facet name="header">
               <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header"/>
            </f:facet>
            <h:outputText id="col4-date" value="#{r.modifiedDate}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <a:dataPager styleClass="pager" />
      </a:richList>
   </h:panelGroup>
   
   <h:panelGroup rendered="#{DialogManager.bean.submitItemsSize != 0}">
      <f:verbatim><% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %></f:verbatim>
      <h:panelGrid columns="2" cellpadding="0" cellpadding="0">
         <h:graphicImage url="/images/icons/info_icon.gif" style="padding-top:2px;padding-right:4px" width="16" height="16"/>
         <h:outputText styleClass="mainSubText" value="#{msg.submit_submit_info}" />
      </h:panelGrid>
      <f:verbatim><% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %></f:verbatim>
      
      <a:richList id="submit-list" viewMode="details" value="#{DialogManager.bean.submitItems}" 
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
                  altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="name">
         
         <%-- Primary column for details view mode --%>
         <a:column id="col10" primary="true" width="100" style="padding:2px;text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <f:facet name="small-icon">
               <a:actionLink id="col10-icon" value="#{r.name}" href="#{r.url}" target="new" image="#{r.icon}" showLink="false" styleClass="inlineAction" />
            </f:facet>
            <a:actionLink id="col10-name" value="#{r.name}" href="#{r.url}" target="new" />
         </a:column>
         
         <%-- Description columns --%>
         <a:column id="col11" width="200" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
            </f:facet>
            <h:outputText id="col11-desc" value="#{r.description}" />
         </a:column>
         
         <%-- Description columns --%>
         <a:column id="col12" style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.path}" value="path" styleClass="header"/>
            </f:facet>
            <h:outputText id="col12-path" value="#{r.path}" />
         </a:column>
         
         <%-- Modified Date column --%>
         <a:column id="col13" style="text-align:left; white-space:nowrap">
            <f:facet name="header">
               <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header"/>
            </f:facet>
            <h:outputText id="col13-date" value="#{r.modifiedDate}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <a:dataPager styleClass="pager" />
      </a:richList>
   </h:panelGroup>
   
</h:panelGrid>

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

<h:panelGroup id="no-metadata-panel" rendered="#{WizardManager.bean.taskMetadataNode == null}">
   <f:verbatim>
      <%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
      <table><tr><td>
   </f:verbatim>
   <h:graphicImage url="/images/icons/info_icon.gif" />
   <f:verbatim>
      </td><td>
   </f:verbatim>
   <h:outputText value="#{msg.start_workflow_no_metadata}" />
   <f:verbatim>
      </td></tr></table>
      <%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
   </f:verbatim>
</h:panelGroup>

<a:panel id="metadata-panel" label="#{msg.properties}" rendered="#{WizardManager.bean.taskMetadataNode != null}"
         border="white" bgcolor="white" 
         titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">
   
   <r:propertySheetGrid id="task-props" value="#{WizardManager.bean.taskMetadataNode}" 
                        var="taskProps" columns="1" externalConfig="true" />
</a:panel>
   
<h:outputText id="padding" styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel id="resources-panel" label="#{msg.resources}"
      border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">

   <a:richList id="resources-list" viewMode="details" value="#{WizardManager.bean.resources}" var="r"
               binding="#{WizardManager.bean.packageItemsRichList}"
               styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
               altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
               initialSortColumn="name" initialSortDescending="true">
      
      <%-- Name column --%>
      <a:column id="col1" primary="true" width="200" style="padding:2px; text-align:left">
         <f:facet name="header">
            <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
         </f:facet>
         <f:facet name="small-icon">
               <a:actionLink id="col1-act1" value="#{r.name}" href="#{r.url}" target="new" 
                             image="#{r.fileType16}" showLink="false" styleClass="inlineAction" />
         </f:facet>
         <a:actionLink id="col1-act2" value="#{r.name}" href="#{r.url}" target="new" />
      </a:column>
      
      <%-- Description column --%>
      <a:column id="col2" style="text-align:left">
         <f:facet name="header">
            <a:sortLink id="col2-sort" label="#{msg.description}" value="description" styleClass="header"/>
         </f:facet>
         <h:outputText id="col2-txt" value="#{r.description}" />
      </a:column>
      
      <%-- Path column --%>
      <a:column id="col3" style="text-align:left">
         <f:facet name="header">
            <a:sortLink id="col3-sort" label="#{msg.path}" value="path" styleClass="header"/>
         </f:facet>
         <r:nodePath id="col3-path" value="#{r.path}" />
      </a:column>
      
      <%-- Created Date column --%>
      <a:column id="col4" style="text-align:left">
         <f:facet name="header">
            <a:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header"/>
         </f:facet>
         <h:outputText id="col4-txt" value="#{r.created}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
      
      <%-- Modified Date column --%>
      <a:column id="col5" style="text-align:left">
         <f:facet name="header">
            <a:sortLink id="col5-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
         </f:facet>
         <h:outputText id="col5-txt" value="#{r.modified}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
                        
      <%-- Actions column --%>
      <a:column id="col6" actions="true" style="text-align:left">
         <f:facet name="header">
            <h:outputText id="col6-txt" value="#{msg.actions}"/>
         </f:facet>
         <r:actions id="col6-actions" value="#{WizardManager.bean.packageItemActionGroup}" 
                    context="#{r}" showLink="false" styleClass="inlineAction" />
      </a:column>
   </a:richList>

   <h:panelGrid id="package-actions-group" columns="1" styleClass="paddingRow">
      <r:actions id="package-actions" context="#{WizardManager.bean.taskMetadataNode}" 
                 value="#{WizardManager.bean.packageActionGroup}" />
   </h:panelGrid>
   
   <h:panelGrid id="add-item-control" columns="1" rendered="#{WizardManager.bean.itemBeingAdded}" 
                styleClass="selector" style="margin-top: 6px;">
      <r:contentSelector id="content-picker" value="#{WizardManager.bean.itemsToAdd}" styleClass="" />
      <h:panelGrid columns="2">
         <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{WizardManager.bean.addPackageItems}" />
         <h:commandButton value="#{msg.cancel}" actionListener="#{WizardManager.bean.cancelAddPackageItems}" />
      </h:panelGrid>
   </h:panelGrid>
   
</a:panel>



         
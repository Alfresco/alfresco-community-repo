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

<h:panelGroup rendered="#{WizardManager.bean.taskMetadataNode == null}">
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

<h:panelGroup rendered="#{WizardManager.bean.taskMetadataNode != null}">
   <a:panel id="props-panel" label="#{msg.properties}" border="white" bgcolor="white" 
            titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">
      
      <r:propertySheetGrid id="task-props" value="#{WizardManager.bean.taskMetadataNode}" 
                           var="taskProps" columns="1" externalConfig="true" />
   </a:panel>
   
   <h:outputText styleClass="paddingRow" value="&nbsp;" escape="false" />
   
   <a:panel id="resources-panel" label="#{msg.resources}"
         border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">
   
      <a:richList id="resources-list" viewMode="details" value="#{WizardManager.bean.resources}" var="r"
                  binding="#{WizardManager.bean.packageItemsRichList}"
                  styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
                  altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
                  initialSortColumn="name" initialSortDescending="true">
         
         <%-- Name column --%>
         <a:column primary="true" width="200" style="padding:2px; text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
            </f:facet>
            <f:facet name="small-icon">
                  <a:actionLink value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" 
                                showLink="false" styleClass="inlineAction" />
            </f:facet>
            <a:actionLink value="#{r.name}" href="#{r.url}" target="new" />
         </a:column>
         
         <%-- Description column --%>
         <a:column style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.description}" />
         </a:column>
         
         <%-- Path column --%>
         <a:column style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.path}" value="path" styleClass="header"/>
            </f:facet>
            <r:nodePath value="#{r.path}" />
         </a:column>
         
         <%-- Created Date column --%>
         <a:column style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.created}" value="created" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.created}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
         
         <%-- Modified Date column --%>
         <a:column style="text-align:left">
            <f:facet name="header">
               <a:sortLink label="#{msg.modified}" value="modified" styleClass="header"/>
            </f:facet>
            <h:outputText value="#{r.modified}">
               <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
            </h:outputText>
         </a:column>
                           
         <%-- Actions column --%>
         <a:column actions="true" style="text-align:left">
            <f:facet name="header">
               <h:outputText value="#{msg.actions}"/>
            </f:facet>
            <r:actions id="actions-col-actions" value="#{WizardManager.bean.packageItemActionGroup}" 
                       context="#{r}" showLink="false" styleClass="inlineAction" />
         </a:column>
      </a:richList>
   
      <h:panelGrid columns="1" styleClass="paddingRow">
         <r:actions context="#{WizardManager.bean.taskMetadataNode}" value="#{WizardManager.bean.packageActionGroup}" />
      </h:panelGrid>
      
      <h:panelGrid columns="1" rendered="#{WizardManager.bean.itemBeingAdded}" styleClass="selector" style="margin-top: 6px;">
         <r:contentSelector value="#{WizardManager.bean.itemsToAdd}" styleClass="" />
         <h:panelGrid columns="2">
            <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{WizardManager.bean.addPackageItems}" />
            <h:commandButton value="#{msg.cancel}" actionListener="#{WizardManager.bean.cancelAddPackageItems}" />
         </h:panelGrid>
      </h:panelGrid>
      
   </a:panel>
</h:panelGroup>



         
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

<a:panel id="props-panel" label="#{msg.workitem_properties}" 
         border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">
   
   <r:propertySheetGrid id="work-item-props" value="#{DialogManager.bean.workItemNode}" 
                       var="workItemProps" columns="1" externalConfig="true" />
</a:panel>

<h:outputText styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel id="resources-panel" label="#{msg.resources}"
         border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle">
   
   <a:richList id="resources-list" viewMode="details" value="#{DialogManager.bean.resources}" var="r"
               binding="#{DialogManager.bean.packageItemsRichList}"
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
         <r:actions id="actions-col-actions" value="workflow_item_collection_actions" 
                    context="#{r}" showLink="false" styleClass="inlineAction" />
      </a:column>
      
      <%-- Completed column --%>
      <%--
      <a:column style="text-align:left">
         <f:facet name="header">
            <h:outputText value="#{msg.completed}" />
         </f:facet>
         <a:actionLink value="#{r.completed}" actionListener="#{DialogManager.bean.togglePackageItemComplete}">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
      </a:column>
      --%>
   </a:richList>
   
   <%-- Put the package actions here --%>
   
</a:panel>

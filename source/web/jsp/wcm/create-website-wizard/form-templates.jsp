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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%">
   <%-- Template selection list --%>
   <h:outputText styleClass="mainSubText" value="#{msg.website_select_templates}:" />
   <h:panelGroup>
      <a:selectList id="template-list" activeSelect="true" styleClass="selectListTable" itemStyleClass="selectListItem">
         <a:listItems value="#{DialogManager.bean.templatesList}" />
         <h:commandButton value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{DialogManager.bean.addTemplate}" />
      </a:selectList>
   </h:panelGroup>
   
   <f:verbatim><div style='padding:4px'></div></f:verbatim>
   <h:outputText styleClass="mainSubText" value="#{msg.website_selected_templates}:" />
   <h:dataTable value="#{DialogManager.bean.templatesDataModel}" var="row" 
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4" 
                rendered="#{DialogManager.bean.templatesDataModel.rowCount != 0}">
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.name}" />
         </f:facet>
         <h:outputText value="#{row.title}" />
      </h:column>
      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.configure}" />
         </f:facet>
         <h:outputText value="#{msg.output_path_pattern}:" style="padding-right:4px" />
         <h:inputText value="#{row.outputPathPattern}" size="65" maxlength="1024" />
      </h:column>
      <h:column>
         <a:actionLink actionListener="#{DialogManager.bean.removeTemplate}" image="/images/icons/delete.gif"
                       value="#{msg.remove}" showLink="false" style="padding-left:6px" />
      </h:column>
   </h:dataTable>
   
   <a:panel id="no-items" rendered="#{DialogManager.bean.templatesDataModel.rowCount == 0}">
      <h:panelGrid columns="1" cellpadding="2" styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
</h:panelGrid>

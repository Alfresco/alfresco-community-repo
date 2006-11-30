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

<h:panelGrid columns="1" cellpadding="2" style="padding-top:2px; padding-bottom:2px;" width="100%">
   <h:outputText styleClass="mainSubText" value="#{msg.specify_usersgroups}" />
   <h:outputText styleClass="mainSubText" value="1. #{msg.select_usersgroups}" />
   <a:genericPicker id="picker" showAddButton="false" filters="#{WizardManager.bean.filters}" queryCallback="#{WizardManager.bean.pickerCallback}" />
   <h:outputText value="#{msg.role}" />
   <h:selectOneListbox id="roles" style="width:250px" size="5">
      <f:selectItems value="#{WizardManager.bean.roles}" />
   </h:selectOneListbox>
   <h:panelGroup styleClass="mainSubText">
      <h:outputText value="2." /> <h:commandButton value="#{msg.add_to_list_button}" actionListener="#{WizardManager.bean.addSelection}" styleClass="wizardButton" />
   </h:panelGroup>
   <h:outputText styleClass="mainSubText" value="#{msg.selected_usersgroups}" />
   <h:panelGroup>
      <h:dataTable value="#{WizardManager.bean.userRolesDataModel}" var="row" 
                   rowClasses="selectedItemsRow,selectedItemsRowAlt"
                   styleClass="selectedItems" headerClass="selectedItemsHeader"
                   cellspacing="0" cellpadding="4" 
                   rendered="#{WizardManager.bean.userRolesDataModel.rowCount != 0}">
         <h:column>
            <f:facet name="header">
               <h:outputText value="#{msg.name}" />
            </f:facet>
            <h:outputText value="#{row.label}" />
         </h:column>
         <h:column>
            <a:actionLink actionListener="#{WizardManager.bean.removeSelection}" image="/images/icons/delete.gif"
                          value="#{msg.remove}" showLink="false" style="padding-left:6px" />
         </h:column>
      </h:dataTable>
      
      <a:panel id="no-items" rendered="#{WizardManager.bean.userRolesDataModel.rowCount == 0}">
         <h:panelGrid columns="1" cellpadding="2" styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
            <h:outputText id="no-items-name" value="#{msg.name}" />
            <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
         </h:panelGrid>
      </a:panel>
   </h:panelGroup>
</h:panelGrid>

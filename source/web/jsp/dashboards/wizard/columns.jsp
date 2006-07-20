<%--
  Copyright (C) 2006 Alfresco, Inc.
 
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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
   <script type="text/javascript">
      window.onload = pageLoaded;
   
      function pageLoaded()
      {
         document.getElementById("wizard:finish-button").disabled = false;
      }
   </script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" cellspacing="0" border="0" width="100%">
   <h:panelGroup rendered="#{WizardManager.bean.columnCount != 1}">
      <h:outputText value="#{msg.select_column}:" />
      <f:verbatim>&nbsp;</f:verbatim>
      <h:selectOneMenu id="columns" value="#{WizardManager.bean.column}" onchange="document.forms['wizard'].submit(); return true;">
         <f:selectItems value="#{WizardManager.bean.columns}" />
      </h:selectOneMenu>
   </h:panelGroup>
   
   <h:panelGrid columns="3" cellpadding="2" cellspacing="0" border="0" columnClasses="alignTop,alignMiddle">
      <h:panelGrid columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.dashlet_list}:" />
         <%-- note this component ID is referenced in DashboardWizard --%>
         <h:selectManyListbox id="all-dashlets" style="width:300px" size="8">
            <f:selectItems value="#{WizardManager.bean.allDashlets}" />
         </h:selectManyListbox>
      </h:panelGrid>
      
      <h:commandButton value="#{msg.dashlet_btn_select} >>" actionListener="#{WizardManager.bean.addDashlets}" />
      
      <h:panelGrid columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.selected_dashlets}:" />
         <h:panelGrid columns="2" cellpadding="2" cellspacing="0" border="0">
            <%-- note this component ID is referenced in DashboardWizard --%>
            <h:selectOneListbox id="column-dashlets" style="width:150px" size="8">
               <f:selectItems value="#{WizardManager.bean.columnDashlets}" />
            </h:selectOneListbox>
            <h:panelGroup>
               <h:commandButton value="+" style="width:24px" actionListener="#{WizardManager.bean.dashletUp}" />
               <f:verbatim><br></f:verbatim>
               <h:commandButton value="-" style="width:24px" actionListener="#{WizardManager.bean.dashletDown}"/>
            </h:panelGroup>
         </h:panelGrid>
         <h:commandButton value="#{msg.dashlet_btn_remove}" actionListener="#{WizardManager.bean.removeDashlet}" />
      </h:panelGrid>
   </h:panelGrid>
</h:panelGrid>

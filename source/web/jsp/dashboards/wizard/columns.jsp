<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
   <script type="text/javascript">
      addEventToElement(window, 'load', pageLoaded, false);
   
      function pageLoaded()
      {
         document.getElementById("wizard:finish-button").disabled = false;
      }
      function outputSelectedItem()
      {
      	 var all_dashlets = document.getElementById('wizard:wizard-body:all-dashlets');
      	 document.getElementById('wizard:wizard-body:selected-dashlets').innerHTML = all_dashlets.options[all_dashlets.selectedIndex].innerHTML;
      }
   </script>
</f:verbatim>

<h:panelGrid id="main-panel" columns="1" cellpadding="2" cellspacing="0" border="0" width="100%">
   <h:panelGroup id="panel1" rendered="#{WizardManager.bean.columnCount != 1}">
      <h:outputText value="#{msg.select_column}:" />
      <f:verbatim>&nbsp;</f:verbatim>
      <h:selectOneMenu id="columns" value="#{WizardManager.bean.column}" onchange="document.forms['wizard'].submit(); return true;">
         <f:selectItems value="#{WizardManager.bean.columns}" />
      </h:selectOneMenu>
      <f:verbatim>&nbsp;</f:verbatim>
      <h:outputFormat value="- #{msg.column_max_components}">
         <f:param value="#{WizardManager.bean.columnMax}" />
      </h:outputFormat>
   </h:panelGroup>

   <h:panelGroup id="panel1b" rendered="#{WizardManager.bean.columnCount == 1}">
      <h:outputFormat value="#{msg.single_column_max_components}">
         <f:param value="#{WizardManager.bean.columnMax}" />
      </h:outputFormat>
   </h:panelGroup>
   
   <h:panelGrid id="panel2" columns="3" cellpadding="2" cellspacing="0" border="0" columnClasses="alignTop,alignMiddle">
      <h:panelGrid id="panel3" columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.dashlet_list}:" />
         <%-- note this component ID is referenced in DashboardWizard --%>
         <h:selectManyListbox id="all-dashlets" style="width:300px" size="8" onchange="outputSelectedItem()">
            <f:selectItems value="#{WizardManager.bean.allDashlets}" />
         </h:selectManyListbox>
      </h:panelGrid>

      <h:commandButton id="select-btn" value="#{msg.dashlet_btn_select} >>" actionListener="#{WizardManager.bean.addDashlets}" disabled="#{WizardManager.bean.columnDashletCount >= WizardManager.bean.columnMax}"/>
      
      <h:panelGrid id="panel4" columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.selected_dashlets}:" />
         <h:panelGrid id="panel5" columns="2" cellpadding="2" cellspacing="0" border="0">
            <%-- note this component ID is referenced in DashboardWizard --%>
            <h:selectOneListbox id="column-dashlets" style="width:150px" size="8">
               <f:selectItems value="#{WizardManager.bean.columnDashlets}" />
            </h:selectOneListbox>
            <h:panelGroup id="panel6">
               <h:commandButton value="+" style="width:24px" actionListener="#{WizardManager.bean.dashletUp}" disabled="#{WizardManager.bean.columnDashletCount < 1}"/>
               <f:verbatim><br></f:verbatim>
               <h:commandButton value="-" style="width:24px" actionListener="#{WizardManager.bean.dashletDown}" disabled="#{WizardManager.bean.columnDashletCount < 1}"/>
            </h:panelGroup>
         </h:panelGrid>
         <h:commandButton id="remove-btn" value="#{msg.dashlet_btn_remove}" actionListener="#{WizardManager.bean.removeDashlet}" disabled="#{WizardManager.bean.columnDashletCount < 1}"/>
      </h:panelGrid>
   </h:panelGrid>
   <h:outputText value="#{msg.selected_item}:" />
   <h:outputText id="selected-dashlets" style="font-style:italic" />
   
</h:panelGrid>

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
   
   <h:panelGrid id="panel2" columns="3" cellpadding="2" cellspacing="0" border="0" columnClasses="alignTop,alignMiddle">
      <h:panelGrid id="panel3" columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.dashlet_list}:" />
         <%-- note this component ID is referenced in DashboardWizard --%>
         <h:selectManyListbox id="all-dashlets" style="width:300px" size="8">
            <f:selectItems value="#{WizardManager.bean.allDashlets}" />
         </h:selectManyListbox>
      </h:panelGrid>
      
      <h:commandButton id="select-btn" value="#{msg.dashlet_btn_select} >>" actionListener="#{WizardManager.bean.addDashlets}" />
      
      <h:panelGrid id="panel4" columns="1" cellpadding="2" border="0">
         <h:outputText value="#{msg.selected_dashlets}:" />
         <h:panelGrid id="panel5" columns="2" cellpadding="2" cellspacing="0" border="0">
            <%-- note this component ID is referenced in DashboardWizard --%>
            <h:selectOneListbox id="column-dashlets" style="width:150px" size="8">
               <f:selectItems value="#{WizardManager.bean.columnDashlets}" />
            </h:selectOneListbox>
            <h:panelGroup id="panel6">
               <h:commandButton value="+" style="width:24px" actionListener="#{WizardManager.bean.dashletUp}" />
               <f:verbatim><br></f:verbatim>
               <h:commandButton value="-" style="width:24px" actionListener="#{WizardManager.bean.dashletDown}"/>
            </h:panelGroup>
         </h:panelGrid>
         <h:commandButton id="remove-btn" value="#{msg.dashlet_btn_remove}" actionListener="#{WizardManager.bean.removeDashlet}" />
      </h:panelGrid>
   </h:panelGrid>
</h:panelGrid>

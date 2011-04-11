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
<%@ taglib uri="/WEB-INF/wcm.tld" prefix="w" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<w:deploymentReports value="#{DialogManager.bean.store}" attempt="#{DialogManager.bean.attempt}" />

<h:panelGroup id="panel-facets">
   <f:facet name="title">
      <a:actionLink id="back-to-last-report" value="#{msg.last_deploy_report}"
                    rendered="#{DialogManager.bean.attempt != null}"
                    image="/images/icons/deployment_report.gif" 
                    actionListener="#{DialogManager.bean.showLastReport}" />
   </f:facet>
</h:panelGroup>

<a:panel id="more-reports-panel" label="#{msg.more_deploy_reports}" progressive="true" facetsId="dialog-body:panel-facets"
         styleClass="mainSubTitle" border="innerwhite" bgcolor="white" titleBgcolor="white" 
         expanded="#{DialogManager.bean.panelExpanded}" expandedActionListener="#{DialogManager.bean.panelToggled}">
   
   <h:outputText value="<div class='deployMoreReportsPanel'>" escape="false" />
   <h:panelGrid id="more-reports-filter" columns="2" styleClass="deployMoreReportsList" width="100%" columnClasses=",rightHandColumn">
      <h:graphicImage value="/images/icons/filter.gif" />
      <a:modeList id="more-reports-filter-list" itemSpacing="2" iconColumnWidth="0" horizontal="true" 
                  selectedLinkStyle="font-weight:bold" value="#{DialogManager.bean.dateFilter}" 
                  actionListener="#{DialogManager.bean.dateFilterChanged}">
         <a:listItem id="f1" value="today" label="#{msg.date_filter_today}" />
         <a:listItem id="f2" value="yesterday" label="#{msg.date_filter_yesterday}" />
         <a:listItem id="f3" value="week" label="#{msg.date_filter_week}" />
         <a:listItem id="f4" value="month" label="#{msg.date_filter_month}" />
         <a:listItem id="f5" value="all" label="#{msg.date_filter_all}" />
      </a:modeList>
   </h:panelGrid>

   <w:deploymentReports id="more-reports-list" value="#{DialogManager.bean.store}" showPrevious="true" 
                        dateFilter="#{DialogManager.bean.dateFilter}" />
   <h:outputText value="</div>" escape="false" />
   
</a:panel>

<h:outputText id="more-reports-bottom-div" value="<div style='padding-top:10px;'></div>" escape="false" />








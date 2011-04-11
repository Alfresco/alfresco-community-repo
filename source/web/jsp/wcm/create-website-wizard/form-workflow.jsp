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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid id="grid-1" columns="1" cellpadding="2" width="100%">
   <r:propertySheetGrid id="task-props" value="#{DialogManager.bean.workflowMetadataNode}" 
         var="taskProps" columns="1" externalConfig="true" />
</h:panelGrid>

<h:panelGroup id="grp-1" rendered="#{DialogManager.bean.filenamePattern != null}">
   <h:panelGrid id="grid-2" columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
         width="100%" rowClasses="wizardSectionHeading">
      <h:outputText id="msg-1" value="&nbsp;#{msg.workflow_settings}" escape="false" />
   </h:panelGrid>
   
   <h:panelGrid id="grid-3" columns="2" cellpadding="2" cellspacing="2" style="margin-left:16px">
      <h:outputText id="msg-2" value="&nbsp;#{msg.website_filename_match}" escape="false" />
      <h:inputText id="in-1" value="#{DialogManager.bean.filenamePattern}" maxlength="1024" size="70" />
   </h:panelGrid>
</h:panelGroup>
<%--
  Copyright (C) 2005 Alfresco, Inc.
 
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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<a:panel id="props-panel" label="#{msg.task_properties}" 
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">
   
   <r:propertySheetGrid id="task-props" value="#{DialogManager.bean.taskNode}"
                       var="taskProps" columns="1" externalConfig="true" mode="view" />
</a:panel>

<h:outputText id="padding1" styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel id="resources-panel" label="#{msg.resources}"
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">
   
   <h:outputText value="#{msg.no_resources}" rendered="#{empty DialogManager.bean.resources}" />
   
   <a:richList id="resources-list" viewMode="details" value="#{DialogManager.bean.resources}" var="r"
               binding="#{DialogManager.bean.packageItemsRichList}"
               styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
               altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
               initialSortColumn="name" initialSortDescending="true"
               rendered="#{not empty DialogManager.bean.resources}">
      
      <%-- Name column --%>
      <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
         </f:facet>
         <f:facet name="small-icon">
               <a:actionLink id="col1-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileType16}" 
                             showLink="false" styleClass="inlineAction" />
         </f:facet>
         <a:actionLink id="col1-act2" value="#{r.name}" href="#{r.url}" target="new" />
         <r:lockIcon id="col1-lock" value="#{r.nodeRef}" align="absmiddle" />
      </a:column>
      
      <%-- Description column --%>
      <a:column id="col2" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col2-sort" label="#{msg.description}" value="description" styleClass="header"/>
         </f:facet>
         <h:outputText id="col2-txt" value="#{r.description}" />
      </a:column>
      
      <%-- Path column --%>
      <a:column id="col3" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col3-sort" label="#{msg.path}" value="path" styleClass="header"/>
         </f:facet>
         <r:nodePath id="col3-path" value="#{r.path}" action="dialog:close:browse" 
                     actionListener="#{BrowseBean.clickSpacePath}" />
      </a:column>
      
      <%-- Created Date column --%>
      <a:column id="col4" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header"/>
         </f:facet>
         <h:outputText id="col4-txt" value="#{r.created}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
      
      <%-- Modified Date column --%>
      <a:column id="col5" style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink id="col5-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
         </f:facet>
         <h:outputText id="col5-txt" value="#{r.modified}">
            <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
         </h:outputText>
      </a:column>
   </a:richList>   
</a:panel>

<h:outputText id="padding2" styleClass="paddingRow" value="&nbsp;" escape="false" />

<a:panel id="workflow-summary-panel" label="#{msg.part_of_workflow}"
         border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle">

   <r:workflowSummary id="workflow-summary" value="#{DialogManager.bean.workflowInstance}" styleClass="workflowSummary" />

</a:panel>

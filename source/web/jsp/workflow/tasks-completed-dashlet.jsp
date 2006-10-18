<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<h:outputText value="#{msg.no_tasks}" rendered="#{empty WorkflowBean.tasksCompleted}" />

<a:richList id="tasks-completed-list" viewMode="details" value="#{WorkflowBean.tasksCompleted}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="bpm:completionDate" initialSortDescending="true"
            rendered="#{not empty WorkflowBean.tasksCompleted}">

   <%-- Primary column for details view mode --%>
   <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col1-sort" label="#{msg.description}" value="name" mode="case-insensitive" styleClass="header"/>
      </f:facet>
      <f:facet name="small-icon">
         <a:actionLink id="col1-act1" value="#{r['bpm:description']}" image="/images/icons/completed_workflow_task.gif" showLink="false"
                       actionListener="#{DialogManager.setupParameters}" action="dialog:viewCompletedTask">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
      </f:facet>
      <a:actionLink id="col1-act2" value="#{r['bpm:description']}" actionListener="#{DialogManager.setupParameters}" 
                    action="dialog:viewCompletedTask">
         <f:param name="id" value="#{r.id}" />
      </a:actionLink>
   </a:column>

   <%-- Task type --%>   
   <a:column id="col2" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col2-sort" label="#{msg.type}" value="name" mode="case-insensitive" styleClass="header"/>
      </f:facet>
      <h:outputText id="col2-txt" value="#{r.name}" />
   </a:column>
   
   <%-- Task id column --%>
   <a:column id="col3" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col3-sort" label="#{msg.id}" value="bpm:taskId" styleClass="header"/>
      </f:facet>
      <h:outputText id="col3-txt" value="#{r['bpm:taskId']}" />
   </a:column>
      
   <%-- Source column --%>
   <a:column id="col4" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col4-sort" label="#{msg.source}" value="sourceSpaceName" styleClass="header"/>
      </f:facet>
      <a:actionLink id="col4-act1" value="#{r.sourceSpaceName}" 
                    actionListener="#{BrowseBean.clickSpace}" action="browse">
         <f:param name="id" value="#{r.sourceSpaceId}" />
      </a:actionLink>
   </a:column>
   
   <%-- Created Date column --%>
   <a:column id="col5" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col5-sort" label="#{msg.created}" value="created" styleClass="header"/>
      </f:facet>
      <h:outputText id="col5-txt" value="#{r.created}">
         <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
      </h:outputText>
   </a:column>
   
   <%-- Completed date column --%>
   <a:column id="col6" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col6-sort" label="#{msg.completed_on}" value="bpm:completionDate" styleClass="header"/>
      </f:facet>
      <h:outputText id="col6-txt" value="#{r['bpm:completionDate']}">
         <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
      </h:outputText>
   </a:column>
   
   <%-- Outcome column --%>
   <a:column id="col7" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col7-sort" label="#{msg.outcome}" value="outcome" styleClass="header"/>
      </f:facet>
      <h:outputText id="col7-txt" value="#{r.outcome}" />
   </a:column>
   
   <%-- Actions column --%>
   <a:column id="col8" actions="true" style="padding:2px;text-align:left">
      <f:facet name="header">
         <h:outputText id="col8-txt" value="#{msg.actions}"/>
      </f:facet>
      <r:actions id="col8-actions" value="dashlet_completed_actions" context="#{r}" showLink="false" 
                 styleClass="inlineAction" />
   </a:column>
   
   <a:dataPager id="pager" styleClass="pager" />
</a:richList>
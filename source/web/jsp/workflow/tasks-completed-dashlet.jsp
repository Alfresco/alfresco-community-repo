<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<a:richList id="tasks-completed-list" viewMode="details" value="#{WorkflowBean.tasksCompleted}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="name" initialSortDescending="true">

   <%-- Primary column for details view mode --%>
   <a:column primary="true" width="200" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.title}" value="name" mode="case-insensitive" styleClass="header"/>
      </f:facet>
      <f:facet name="small-icon">
         <a:actionLink value="#{r.name}" image="/images/icons/completed_workflow_task.gif" showLink="false"
                       actionListener="#{DialogManager.setupParameters}" action="dialog:viewCompletedTask">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
      </f:facet>
      <a:actionLink value="#{r.name}" actionListener="#{DialogManager.setupParameters}" 
                    action="dialog:viewCompletedTask">
         <f:param name="id" value="#{r.id}" />
      </a:actionLink>
   </a:column>
   
   <%-- Task id column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.id}" value="bpm:taskId" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r['bpm:taskId']}" />
   </a:column>
   
   <%-- Source column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.source}" value="sourceSpaceName" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r.sourceSpaceName}" />
   </a:column>
   
   <%-- Completed date column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.completed_on}" value="bpm:completionDate" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r['bpm:completionDate']}">
         <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
      </h:outputText>
   </a:column>
   
   <%-- Outcome column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.outcome}" value="outcome" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r.outcome}" />
   </a:column>
   
   <%-- Actions column --%>
   <a:column actions="true" style="text-align:left">
      <f:facet name="header">
         <h:outputText value="#{msg.actions}"/>
      </f:facet>
      <r:actions value="dashlet_completed_actions" context="#{r}" showLink="false" 
                 styleClass="inlineAction" />
   </a:column>
   
   <a:dataPager styleClass="pager" />
</a:richList>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<a:richList id="tasks-todo-list" viewMode="details" value="#{WorkflowBean.tasksToDo}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="name" initialSortDescending="true">

   <%-- Primary column for details view mode --%>
   <a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink id="col1-sort" label="#{msg.title}" value="name" mode="case-insensitive" styleClass="header"/>
      </f:facet>
      <f:facet name="small-icon">
         <a:actionLink id="col1-act1" value="#{r.name}" image="/images/icons/workflow_task.gif" showLink="false"
                       actionListener="#{DialogManager.setupParameters}" action="dialog:manageTask">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
      </f:facet>
      <a:actionLink id="col1-act2" value="#{r.name}" actionListener="#{DialogManager.setupParameters}" 
                    action="dialog:manageTask">
         <f:param name="id" value="#{r.id}" />
      </a:actionLink>
   </a:column>
   
   <%-- Task id column --%>
   <a:column id="col2" style="text-align:left">
      <f:facet name="header">
         <a:sortLink id="col2-sort" label="#{msg.id}" value="bpm:taskId" styleClass="header"/>
      </f:facet>
      <h:outputText id="col2-txt" value="#{r['bpm:taskId']}" />
   </a:column>
   
   <%-- Source column --%>
   <a:column id="col3" style="text-align:left">
      <f:facet name="header">
         <a:sortLink id="col3-sort" label="#{msg.source}" value="sourceSpaceName" styleClass="header"/>
      </f:facet>
      <h:outputText id="col3-txt" value="#{r.sourceSpaceName}" />
   </a:column>
   
   <%-- Due date column --%>
   <a:column id="col4" style="text-align:left">
      <f:facet name="header">
         <a:sortLink id="col4-sort" label="#{msg.due_date}" value="bpm:startDate" styleClass="header"/>
      </f:facet>
      <h:outputText id="col4-txt" value="#{r['bpm:dueDate']}">
         <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
      </h:outputText>
   </a:column>
   
   <%-- Status column --%>
   <a:column id="col5" style="text-align:left">
      <f:facet name="header">
         <a:sortLink id="col5-sort" label="#{msg.status}" value="bpm:status" styleClass="header"/>
      </f:facet>
      <h:outputText id="col5-txt" value="#{r['bpm:status']}" />
   </a:column>
   
   <%-- Priority column --%>
   <a:column id="col6" style="text-align:left">
      <f:facet name="header">
         <a:sortLink id="col6-sort" label="#{msg.priority}" value="bpm:priority" styleClass="header"/>
      </f:facet>
      <h:outputText id="col6-txt" value="#{r['bpm:priority']}" />
   </a:column>
   
   <%-- Actions column --%>
   <a:column id="col7" actions="true" style="text-align:left">
      <f:facet name="header">
         <h:outputText id="col7-txt" value="#{msg.actions}"/>
      </f:facet>
      <r:actions id="col7-actions" value="dashlet_todo_actions" context="#{r}" showLink="false" 
                 styleClass="inlineAction" />
   </a:column>
   
   <a:dataPager id="pager" styleClass="pager" />
</a:richList>
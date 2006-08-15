<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<a:richList id="workt-items-list" viewMode="details" value="#{WorkflowBean.workItemsToDo}" var="r"
            styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
            altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
            initialSortColumn="name" initialSortDescending="true">

   <%-- Primary column for details view mode --%>
   <a:column primary="true" width="200" style="padding:2px;text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.title}" value="name" mode="case-insensitive" styleClass="header"/>
      </f:facet>
      <f:facet name="small-icon">
         <h:panelGroup>
            <a:actionLink value="#{r.name}" image="/images/icons/View_details.gif" showLink="false"
                          actionListener="#{DialogManager.setupParameters}" action="dialog:manageWorkItem">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </h:panelGroup>
      </f:facet>
      <a:actionLink value="#{r.name}" actionListener="#{DialogManager.setupParameters}" 
                    action="dialog:manageWorkItem">
         <f:param name="id" value="#{r.id}" />
      </a:actionLink>
   </a:column>
   
   <%-- Description column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.id}" value="bpm:taskId" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r['bpm:taskId']}" />
   </a:column>
   
   <%-- Type column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.type}" value="type" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r.type}" />
   </a:column>
   
   <%-- Due date column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.due_date}" value="bpm:startDate" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r['bpm:dueDate']}">
         <a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
      </h:outputText>
   </a:column>
   
   <%-- Status column --%>
   <a:column style="text-align:left">
      <f:facet name="header">
         <a:sortLink label="#{msg.status}" value="bpm:status" styleClass="header"/>
      </f:facet>
      <h:outputText value="#{r['bpm:status']}" />
   </a:column>
   
   <%-- Actions column --%>
   <%--
   <a:column actions="true" style="text-align:left">
      <f:facet name="header">
         <h:outputText value="#{msg.actions}"/>
      </f:facet>
      <a:booleanEvaluator value="#{r.local}">
         <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif" showLink="false" 
                       styleClass="inlineAction"
                       actionListener="#{RulesBean.setupRuleAction}" action="deleteRule">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
         <a:actionLink value="#{msg.change_details}" image="/images/icons/change_rule.gif" 
                       showLink="false" styleClass="inlineAction"
                       actionListener="#{RulesBean.setupRuleAction}" action="wizard:editRule">
            <f:param name="id" value="#{r.id}" />
         </a:actionLink>
      </a:booleanEvaluator>
   </a:column>
   --%>
   
   <a:dataPager styleClass="pager" />
</a:richList>

<h:message for="workt-items-list" styleClass="statusMessage" />
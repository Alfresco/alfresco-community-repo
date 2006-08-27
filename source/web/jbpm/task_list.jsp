<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<h:dataTable id="taskinstances" value="#{homeBean.taskInstancesModel}" var="taskInstance" headerClass="tableheader" columnClasses="tablecell">
  <h:column id="colTISelect">
    <f:facet name="header">
      <h:outputText value="Task" />
    </f:facet>
    <h:commandLink id="selectTaskInstance" action="#{homeBean.selectTaskInstance}">
      <h:outputText id="selectOutTaskInstance" value="#{taskInstance.name}" />
    </h:commandLink>
  </h:column>
  <h:column id="colTIProcessName">
    <f:facet name="header">
      <h:outputText value="Workflow" />
    </f:facet>
    <h:outputText id="colOutTIProcessName" value="#{taskInstance.taskMgmtInstance.taskMgmtDefinition.processDefinition.name}" />
  </h:column>
</h:dataTable> 


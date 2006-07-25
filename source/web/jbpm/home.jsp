<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>

<jsp:include page="header1.jsp" />
Home
<jsp:include page="header2.jsp" />

<b><h:messages /></b>

<h4>Tasklist</h4>
<h:dataTable id="taskinstances" value="#{homeBean.taskInstancesModel}" var="taskInstance" headerClass="tableheader" columnClasses="tablecell">
  <h:column id="colTISelect">
    <f:facet name="header">
      <h:outputText value="Task Form Link" />
    </f:facet>
    <h:commandLink id="selectTaskInstance" action="#{homeBean.selectTaskInstance}">
      <h:outputText id="selectOutTaskInstance" value="#{taskInstance.name}" />
    </h:commandLink>
  </h:column>
  <h:column id="colTIProcessName">
    <f:facet name="header">
      <h:outputText value="Process" />
    </f:facet>
    <h:outputText id="colOutTIProcessName" value="#{taskInstance.taskMgmtInstance.taskMgmtDefinition.processDefinition.name}" />
  </h:column>
  <h:column id="colTIProcessVer">
    <f:facet name="header">
      <h:outputText value="Version" />
    </f:facet>
    <h:outputText id="colOutTIProcessVer" value="#{taskInstance.taskMgmtInstance.taskMgmtDefinition.processDefinition.version}" />
  </h:column>
</h:dataTable> 

<h4>Start New Process Execution</h4>
<h:dataTable id="processdefs" value="#{homeBean.latestProcessDefinitionsModel}" var="processDefinition" headerClass="tableheader" columnClasses="tablecell">
  <h:column id="colProcessLink">
    <f:facet name="header">
      <h:outputText value="Start Process Link" />
    </f:facet>
    <h:commandLink id="startProcessInstance1" rendered="#{processDefinition.taskMgmtDefinition.startTask != null}" action="#{homeBean.startProcessInstance}">
      <h:outputText id="colOutProcessLink1" value="#{processDefinition.taskMgmtDefinition.startTask.name}" />
    </h:commandLink>
    <h:commandLink id="startProcessInstance2" rendered="#{processDefinition.taskMgmtDefinition.startTask == null}" action="#{homeBean.startProcessInstance}">
      <h:outputText id="colOutProcessLink2" value="start process" />
    </h:commandLink>
  </h:column>
  <h:column id="colProcess">
    <f:facet name="header">
      <h:outputText value="Process" />
    </f:facet>
    <h:outputText id="colOutProcess" value="#{processDefinition.name}" />
  </h:column>
  <h:column id="colVersion">
    <f:facet name="header">
      <h:outputText value="Version" />
    </f:facet>
    <h:outputText id="colOutVersion" value="#{processDefinition.version}" />
  </h:column>
</h:dataTable> 

<jsp:include page="footer.jsp" />
</f:view>

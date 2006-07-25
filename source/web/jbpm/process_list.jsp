<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


<f:verbatim>&nbsp;</f:verbatim>
<h:outputLink value="/alfresco/faces/jbpm/home.jsp"><h:outputText value="jBPM Console"/> </h:outputLink>
<f:verbatim><br><br></f:verbatim>

<h:dataTable id="processdefs" value="#{homeBean.latestProcessDefinitionsModel}" var="processDefinition">
  <h:column id="colProcessLink">
    <f:facet name="header">
      <h:outputText value="Start Task" />
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
      <h:outputText value="Workflow" />
    </f:facet>
    <h:outputText id="colOutProcess" value="#{processDefinition.name}" />
  </h:column>
</h:dataTable> 


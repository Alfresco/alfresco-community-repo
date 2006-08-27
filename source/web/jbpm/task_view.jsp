<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/jbpm.tld" prefix="jbpm" %>
<%@ page import="org.jbpm.webapp.bean.*" %>
<%@ page import="org.jbpm.taskmgmt.exe.*" %>

<%@ page isELIgnored="false" %>


<f:view>

<b><h:messages/></b>

<table cellspacing="0" cellpadding="0" border="0"><tr><td valign="top">
<h:form id="taskform">

<h:inputHidden id="taskInstanceId" value="#{taskBean.taskInstanceId}" />

  <h2><h:outputText id="hiddenInstanceName" value="#{taskBean.taskInstance.name}" /></h2>

  <hr />

  <h:dataTable id="formParameters" value="#{taskBean.taskFormParameters}" var="formParameter">
    <h:column>
      <h:outputText id="formLabel" value="#{formParameter.label}" />
    </h:column>
    <h:column>
      <h:outputText id="formDescription" value="#{formParameter.description}" />
    </h:column>
      <h:column>
        <h:inputText id="formValue" value="#{formParameter.value}" readonly="#{formParameter.readOnly}" />
      </h:column>
  </h:dataTable>

  <hr />

 <h:panelGroup id="panelTransitions" rendered="#{taskBean.transitions != null}">
  <h:outputText value="Task Actions:" />
  <h:dataTable id="tableTransitions" value="#{taskBean.transitions}" var="transition">
  <h:column id="colTransition"> 
      <h:commandLink id="transitionButton" action="#{taskBean.saveAndClose}" value="#{transition.name}">
      </h:commandLink>
  </h:column>
  </h:dataTable> 
 </h:panelGroup>
  <h:panelGroup id="panelSaveAndClose" rendered="#{taskBean.transitions == null}">
     <h:outputText value="Task Actions:" />
     <f:verbatim><br/>&nbsp;</f:verbatim>
      <h:commandLink id="transitionButton" action="#{taskBean.saveAndClose}" value="Done"/> 
  </h:panelGroup>
  

  <hr />
  
  <h:commandButton action="#{taskBean.save}" value="Save"/> 
  <h:commandButton action="home" value="Cancel"/>   

</h:form>

</td><td valign="top">
  &nbsp;&nbsp;
</td><td valign="top">
   <jbpm:processimage task="${taskBean.taskInstanceId}"/>
</td>

</tr>
</table>

</f:view>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view locale="#{facesContext.externalContext.request.locale}">
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Monitoring
<jsp:include page="header2.jsp" />

<b><h:messages/></b>

	<br />
	<b><h:outputText value="#{monitoringBean.message}" /></b>
	<br /><br />
	
    <h:commandLink action="#{monitoringBean.showProcessDefinitions}">
      <h:outputText value="#{msgs.processDefinitionsList}" />
    </h:commandLink>
    <br /><br />
    <h:commandLink action="#{monitoringBean.showSearchInstances}">
	  <h:outputText value="#{msgs.searchProcessInstances}"/>
	</h:commandLink>  	
	<br /><br />
    <h:form> 
		Instance ID:  
		<h:inputText value="#{monitoringBean.processInstanceId}" />
		<h:commandButton value="Inspect" action="#{monitoringBean.inspectInstance}" />
	</h:form>
	    
    
<jsp:include page="footer.jsp" />
</f:view>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/jbpm.tld" prefix="jbpm" %>

<f:view locale="#{facesContext.externalContext.request.locale}">
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Transitions
<jsp:include page="header2.jsp" />

<b><h:messages/></b>

<br />
<h:outputText value="Process Instance Id"/>: 
<b><h:commandLink styleClass="standard" value="#{processInstanceBean.id}" action="#{processInstanceBean.inspectProcessInstance}" /></b>
<br />
<h:outputText value="Task Instance Id"/>: 
<b><h:outputText value="#{processInstanceBean.taskInstanceId}" /></b>
<br />
<br />


<hr>

	<h4>Available Transitions</h4>

	<h:dataTable id="transitions" value="#{processInstanceBean.transitions}" var="transition" headerClass="tableheader" columnClasses="tablecell" >

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="Id"/> 
		    </f:facet> 		
			<h:outputText value="#{transition.id}"/>
	 	</h:column>
	 	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="Name"/> 
		    </f:facet> 		
			<h:outputText value="#{transition.name}"/>
	 	</h:column>

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="To"/> 
		    </f:facet> 		
			<h:outputText value="#{transition.to.name}"/>
	 	</h:column>

		<h:column>
			<h:commandLink action="#{processInstanceBean.selectTransition}" >
				<h:outputText value="Select"/>
				<f:param name="transitionName" value="#{transition.name}"/>
			</h:commandLink>  		    
	 	</h:column>
	 		 		 	
	</h:dataTable>	

	<br /><br />
	
	<h:commandLink action="#{processInstanceBean.selectTransition}" >
		<h:outputText value="Default Transition"/>
		<f:param name="transitionName" value=""/>
	</h:commandLink>  		    
	

<jsp:include page="footer.jsp" />
</f:view>

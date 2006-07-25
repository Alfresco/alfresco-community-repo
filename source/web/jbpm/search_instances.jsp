<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view locale="#{facesContext.externalContext.request.locale}">
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Search Instances
<jsp:include page="header2.jsp" />

<b><h:messages showDetail="true" /></b>

    <h:form>
		<h:outputText value="#{msgs.variableName}" />  
		<h:selectOneMenu value="#{monitoringBean.variableNameOperator}">
		  <f:selectItems value="#{monitoringBean.operatorsList}" />
		</h:selectOneMenu>		
		<h:inputText id="Variable_name" required="true" value="#{monitoringBean.variableName}" />
		<br />
		<h:outputText value="#{msgs.variableValue}" />
		<h:selectOneMenu value="#{monitoringBean.variableValueOperator}">
		  <f:selectItems value="#{monitoringBean.operatorsList}" />
		</h:selectOneMenu>			
		<h:inputText id="Variable_value" required="true" value="#{monitoringBean.variableValue}" />
		<br />
		<h:commandButton value="#{msgs.search}" action="#{monitoringBean.searchInstances}" />
		<br /><br />
		<h:outputText value="#{monitoringBean.message}"/>
	</h:form>

	<hr>
	<br />
		
	<h:dataTable id="showProcessInstances" rendered="#{monitoringBean.showProcessInstances}" value="#{monitoringBean.processInstances}" var="processInstance" headerClass="tableheader" columnClasses="tablecell" >

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="Id"/> 
		    </f:facet> 
			<h:commandLink action="#{processInstance.inspectProcessInstance}">
				<h:outputText value="#{processInstance.id}"/>
			</h:commandLink>  
	 	</h:column>			

		<h:column rendered="#{monitoringBean.variableNameOperator == 'like'}">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.variableName}"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.variableName}" />
		</h:column>
	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.variableValue}"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.variableValue}" />
		</h:column>
	 	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="Start Date"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.start}">
				<f:convertDateTime type="both"/>
			</h:outputText>	 	
		</h:column>
	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="End Date"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.end}">
				<f:convertDateTime type="both"/>
			</h:outputText>	 	
		</h:column>
	
	</h:dataTable>		
	
<jsp:include page="footer.jsp" />
</f:view>

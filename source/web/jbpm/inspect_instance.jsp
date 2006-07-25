<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/jbpm.tld" prefix="jbpm" %>

<%@ page isELIgnored="false" %>

<f:view locale="#{facesContext.externalContext.request.locale}">
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Inspect Instance
<jsp:include page="header2.jsp" />

<b><h:messages/></b>

<br />
<i><h:commandLink styleClass="standard" value="Refresh" action="#{processInstanceBean.inspectProcessInstance}" style="float: right;" ><h:outputText value=" page" /></h:commandLink></i>
<h:outputText value="Process Instance Id"/>: 
<b><h:commandLink styleClass="standard" value="#{processInstanceBean.id}" action="#{processInstanceBean.inspectProcessInstance}" /></b>
<br />
<h:outputText value="Process Definition"/>: 
<b><h:commandLink value="#{processInstanceBean.processDefinitionLabel}" action="#{processInstanceBean.showProcessInstances}" /></b>
<br />
<h:outputText value="Process Instance Start"/>: 
<b>
	<h:outputText value="#{processInstanceBean.start}">
		<f:convertDateTime type="both"/>
	</h:outputText>
</b>
<br />
<h:outputText value="Process Instance End"/>: 
<b>
	<h:outputText value="#{processInstanceBean.end}">
		<f:convertDateTime type="both"/>
	</h:outputText>
</b>
<br />

<hr>

	<h4>Tasks</h4>
	
	<h:dataTable id="tasks" value="#{processInstanceBean.tasks}" var="task" headerClass="tableheader" columnClasses="tablecell" >

		<h:column id="colTaskId">
		    <f:facet name="header"> 
		        <h:outputText value="Id"/> 
		    </f:facet> 		
			<h:outputText id="colOutTaskId" value="#{task.taskInstanceId}"/>
	 	</h:column>
	 	
		<h:column id="colTaskName">
		    <f:facet name="header"> 
		        <h:outputText value="Name"/> 
		    </f:facet> 		
			<h:outputText id="colOutTaskName" value="#{task.name}"/>
	 	</h:column>
	 		 	
		<h:column id="colTaskActor">
		    <f:facet name="header"> 
		        <h:outputText value="ActorId"/> 
		    </f:facet> 		
			<h:outputText id="colOutTaskActor" value="#{task.actorId}"/>
	 	</h:column>

		<h:column id="colTaskDate">
		    <f:facet name="header"> 
		        <h:outputText value="Date"/> 
		    </f:facet> 		
			<h:outputText id="colOutTaskDate" value="#{task.end}">
				<f:convertDateTime type="both"/>
			</h:outputText>
	 	</h:column>

		<h:column id="colTaskEndTask">
			<h:commandLink id="endTask" rendered="#{task.ended}" action="#{processInstanceBean.endTask}" >
				<h:outputText id="colOutEndTask" value="End Task"/>
				<f:param name="taskInstanceId" value="#{task.taskInstanceId}"/>
			</h:commandLink>  		    
	 	</h:column>

	</h:dataTable>	

	<h4>Variables</h4>
	
	<h:dataTable id="variables" value="#{processInstanceBean.variables}" var="variable" headerClass="tableheader" columnClasses="tablecell" >
	 	
		<h:column id="colName">
		    <f:facet name="header"> 
		        <h:outputText id="colOutName" value="#{msgs.name}"/> 
		    </f:facet> 		
			<h:outputText value="#{variable.name}"/>
	 	</h:column>
	 	
		<h:column id="colVal">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.value}"/> 
		    </f:facet> 		
			<h:outputText id="colOutVal" value="#{variable.value}"/>
	 	</h:column>

	</h:dataTable>	

	<br />
    <h:form>
		Variable Name:  
		<h:inputText id="varName" value="#{processInstanceBean.variableName}" />
		<br />
		Variable Value:  
		<h:inputText id="varValue" value="#{processInstanceBean.variableValue}" />
		<br />
		<h:commandButton id="updateVar" value="Update" action="#{processInstanceBean.updateVariable}" />
		<br />

	</h:form>
	
	<h4>Tokens</h4>
	
	<h:dataTable id="tokens" value="#{processInstanceBean.tokens}" var="token" headerClass="tableheader" columnClasses="tablecell" >

	 	
		<h:column id="colPIBLabel">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.name}"/> 
		    </f:facet> 		
			<h:outputText id="colOutPIBLabel" value="#{token.label}"/>
	 	</h:column>
	 	
		<h:column id="colPIBID">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.id}"/> 
		    </f:facet> 	
			<h:outputText id="colOutPIBID" value="#{token.id}"/>
	 	</h:column>

		<h:column id="colPIBName">
		    <f:facet name="header"> 
		        <h:outputText value="Node name"/> 
		    </f:facet> 		
		    <h:commandLink id="selectToken" rendered="#{token.id != processInstanceBean.tokenInstanceId}" action="#{processInstanceBean.selectToken}">
				<h:outputText id="colOutSelectToken" value="#{token.nodeName}"/>
				<f:param name="tokenInstanceId" value="#{token.id}"/>
			</h:commandLink>
			<h:panelGroup id="panelToken" rendered="#{token.id == processInstanceBean.tokenInstanceId}">
				<f:verbatim><b></f:verbatim>
				<h:outputText id="colOutPIBName" value="#{token.nodeName}"/>			
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>
	 	</h:column>

		<h:column id="colPIBType">
		    <f:facet name="header"> 
		        <h:outputText value="Node type"/> 
		    </f:facet> 		
			<h:outputText id="colOutPIBType" value="#{token.nodeType}"/>
	 	</h:column>

		<h:column id="colPIBStart">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.start}"/> 
		    </f:facet> 		
			<h:outputText id="colOutPIBStart" value="#{token.start}">
				<f:convertDateTime type="both"/>
			</h:outputText>
	 	</h:column>

		<h:column id="colPIBEnd">
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.end}"/> 
		    </f:facet> 		
			<h:outputText id="colOutPIBEnd" value="#{token.end}">
				<f:convertDateTime type="both"/>
			</h:outputText>
	 	</h:column>

		<h:column id="colPIBSendSignal">
			<h:commandLink id="signal" rendered="#{token.signal}" action="#{processInstanceBean.signal}" >
				<h:outputText id="colOutPIBSendSignal" value="Send Signal"/>
				<f:param name="tokenInstanceId" value="#{token.id}"/>
			</h:commandLink>  		    
	 	</h:column>
	</h:dataTable>	

	<br /><br />
     <jbpm:processimageToken token="${processInstanceBean.tokenInstanceId}"/>

	

<jsp:include page="footer.jsp" />
</f:view>

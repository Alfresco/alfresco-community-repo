<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Process Instances
<jsp:include page="header2.jsp" />

<b><h:messages/></b>

<br />

<h:outputText value="Process Definition Id"/>: 
<b><h:outputText value="#{processDefinitionBean.id}"/></b>
<br />

<h:outputText value="Process Definition Name"/>: 
<b><h:outputText value="#{processDefinitionBean.name}"/></b>
<br />

<h:outputText value="Process Definition Version"/>: 
<b><h:outputText value="#{processDefinitionBean.version}"/></b>
<br />

<hr>
	
	<h4>Instances</h4>
	<br />
	
	<h:dataTable id="processinstances" value="#{processDefinitionBean.processInstances}" var="processInstance" headerClass="tableheader" columnClasses="tablecell" >

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.id}"/> 
		    </f:facet> 		
			<h:commandLink action="#{processInstance.inspectProcessInstance}" >
				<h:outputText value="#{processInstance.id}"/>	
			</h:commandLink>  
	 	</h:column>
	 
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.start}"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.start}">
				<f:convertDateTime type="both"/>
			</h:outputText>
	 	</h:column>
	  
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.end}"/> 
		    </f:facet> 		
			<h:outputText value="#{processInstance.end}">
				<f:convertDateTime type="both"/>
			</h:outputText>			
	 	</h:column>

		<h:column>
			<h:commandLink action="#{processInstance.deleteProcessInstance}" >
				<h:outputText value="Delete"/>	
			</h:commandLink>  
	 	</h:column>
	  
	 </h:dataTable>


<jsp:include page="footer.jsp" />
</f:view>

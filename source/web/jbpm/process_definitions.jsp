<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>
<f:loadBundle basename="alfresco.messages.jbpm" var="msgs"/>

<jsp:include page="header1.jsp" />
Process Definitions
<jsp:include page="header2.jsp" />

<b><h:messages/></b>

	<h:dataTable id="processdefs" value="#{monitoringBean.processDefinitions}" var="processDefinition" headerClass="tableheader" columnClasses="tablecell" >

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.id}"/> 
		    </f:facet> 		
			<h:outputText value="#{processDefinition.id}"/>
	 	</h:column>
	 	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.name}"/> 
		    </f:facet> 		
			<h:outputText value="#{processDefinition.name}"/>
	 	</h:column>
	
		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.version}"/> 
		    </f:facet> 		
			<h:outputText value="#{processDefinition.version}"/>
	 	</h:column>

		<h:column>
		    <f:facet name="header"> 
		        <h:outputText value="#{msgs.instances}"/> 
		    </f:facet> 		
		    
			<h:commandLink rendered="#{processDefinition.instancesCount > 0}" 
						   action="#{processDefinition.showProcessInstances}" >
				<h:outputText value="#{processDefinition.instancesCount}"/>	
			</h:commandLink>  		
			    
			<h:outputText rendered="#{processDefinition.instancesCount == 0}" value="0"/>			
			
	 	</h:column>
	
	</h:dataTable>

<jsp:include page="footer.jsp" />

</f:view>

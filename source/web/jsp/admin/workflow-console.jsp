<%--
  Copyright (C) 2005 Alfresco, Inc.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_workflow_console">

<f:view>
   
	<%-- load a bundle of properties with I18N strings --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

	<h:form id="workflow-console-title">
	
		<table width="100%">
			<tr>
	        	<td>
	            	<h:graphicImage value="/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
	         	</td>
	         	<td>
	            	<nobr><h:outputText id="titleWFConsole" styleClass="mainTitle" value="#{msg.title_workflow_console}"/></nobr>
	         	</td>
				<td width="100%" align="right">
					<h:commandButton value="#{msg.close}" action="adminConsole" />
				</td>
	      	</tr>
	   	</table>
	
	</h:form>

	<br>

	<h:outputText id="contextTitle" styleClass="mainTitle" value="#{msg.workflow_context}"/>

	<table>
    	<tr>
        	<td><b>User:</b></td><td><h:outputText id="userName" value="#{WorkflowConsoleBean.currentUserName}"/></td>
		</tr>
		<tr>
			<td><b>Workflow Definition:</b></td><td><h:outputText id="workflowDef" value="#{WorkflowConsoleBean.currentWorkflowDef}"/></td>
		</tr>
	</table>

	<br>

	<h:outputText id="commandTitle" styleClass="mainTitle" value="#{msg.workflow_command}"/>

	<h:form id="searchForm">
		<table>
        	<tr>
				<td>
					<h:inputText id="command" size="100" value="#{WorkflowConsoleBean.command}"/>
				</td>
				<td>
					<h:commandButton id="submitCommand" action="#{WorkflowConsoleBean.submitCommand}" value="#{msg.workflow_command_submit}"/>
				</td>
			</tr>
		</table>
	</h:form>

	<table>
		<tr>
			<td>
				<h:outputText id="submittedCommandLabel" value="#{msg.workflow_last_command}"/> <h:outputText id="submittedCommand" value="#{WorkflowConsoleBean.submittedCommand}"/><br>
				<h:outputText id="durationLabel" value="#{msg.workflow_duration}"/> <h:outputText id="duration" value="#{WorkflowConsoleBean.duration}"/><h:outputText id="durationMsLabel" value="#{msg.workflow_duration_ms}"/><br>
				-----
			</td>
		</tr>
		<tr>
			<td>
				<pre><h:outputText id="result" value="#{WorkflowConsoleBean.result}"/></pre>
			</td>
		</tr>
	</table>

</f:view>

</r:page>

<script>
   document.getElementById("searchForm:command").focus();
</script>

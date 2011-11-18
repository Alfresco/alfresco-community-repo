<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_configadmin_console">

<f:view>
   
	<%-- load a bundle of properties with I18N strings --%>
	<r:loadBundle var="msg"/>

	<h:form id="ConfigAdmin-console-title">
	
		<table width="100%">
			<tr>
	        	<td>
	            	<h:graphicImage value="/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
	         	</td>
	         	<td>
	            	<nobr><h:outputText id="titleConfigAdminConsole" styleClass="mainTitle" value="#{msg.title_configadmin_console}"/></nobr>
	         	</td>
				<td width="100%" align="right">
					<h:commandButton value="#{msg.close}" action="dialog:close" />
				</td>
	      	</tr>
	   	</table>
	
	</h:form>

	<br>
    
    <h:outputText id="contextTitle" styleClass="mainTitle" value="#{msg.configadmin_context}"/>

    <table>
        <tr>
            <td><b>User:</b></td><td><h:outputText id="userName" value="#{ConfigAdminConsoleBean.currentUserName}"/></td>
        </tr>
    </table>

    <br>

    <h:outputText id="commandTitle" styleClass="mainTitle" value="#{msg.configadmin_command}"/>

	<h:form id="searchForm">
		<table>
        	<tr>
				<td>
                    <h:inputText id="command" size="100" value="#{ConfigAdminConsoleBean.command}"/>
				</td>
				<td>
                    <h:commandButton id="submitCommand" action="#{ConfigAdminConsoleBean.submitCommand}" value="#{msg.configadmin_command_submit}"/>
				</td>
			</tr>
		</table>
	</h:form>

	<table>
		<tr>
			<td>
                <h:outputText id="submittedCommandLabel" value="#{msg.configadmin_last_command}"/> <h:outputText id="submittedCommand" value="#{ConfigAdminConsoleBean.submittedCommand}"/><br>
                <h:outputText id="durationLabel" value="#{msg.configadmin_duration}"/> <h:outputText id="duration" value="#{ConfigAdminConsoleBean.duration}"/><h:outputText id="durationMsLabel" value="#{msg.configadmin_duration_ms}"/><br>
				-----
			</td>
		</tr>
		<tr>
			<td>
                <pre><h:outputText id="result" value="#{ConfigAdminConsoleBean.result}"/></pre>
			</td>
		</tr>
	</table>

</f:view>

</r:page>

<script>
   document.getElementById("searchForm:command").focus();
</script>

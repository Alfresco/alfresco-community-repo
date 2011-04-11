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

<f:verbatim>
<script type="text/javascript">
	addEventToElement(window, 'load', pageLoaded, false);

	function pageLoaded()
	{
		document.getElementById("wizard:wizard-body:firstName").focus();
		updateButtonState();
	}
	
	function updateButtonState()
	{
		if (document.getElementById("wizard:wizard-body:firstName").value.length == 0 ||
   		 document.getElementById("wizard:wizard-body:lastName").value.length == 0 ||
   		 document.getElementById("wizard:wizard-body:email").value.length == 0)
		{
			document.getElementById("wizard:next-button").disabled = true;			
		}
		else
		{
			document.getElementById("wizard:next-button").disabled = false;
		}
	}
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.person_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="2" cellspacing="2" width="100%">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.first_name}:"/>
   <h:inputText id="firstName" value="#{WizardManager.bean.firstName}" disabled="#{WizardManager.bean.personPropertiesImmutability.firstName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.last_name}:"/>
   <h:inputText id="lastName" value="#{WizardManager.bean.lastName}" disabled="#{WizardManager.bean.personPropertiesImmutability.lastName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.email}"/>
   <h:inputText id="email" value="#{WizardManager.bean.email}" disabled="#{WizardManager.bean.personPropertiesImmutability.email}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.other_options}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" cellspacing="2" width="100%">
   <h:outputText value="#{msg.company_id}:"/>
   <h:inputText value="#{WizardManager.bean.companyId}" size="35" maxlength="1024" />
   
   <h:outputText value="#{msg.user_organization}:"/>
   <h:inputText id="organisation" value="#{WizardManager.bean.organization}" disabled="#{WizardManager.bean.personPropertiesImmutability.organization}" size="35" maxlength="1024" />
   
   <h:outputText value="#{msg.user_jobtitle}:"/>
   <h:inputText id="jobtitle" value="#{WizardManager.bean.jobtitle}" disabled="#{WizardManager.bean.personPropertiesImmutability.jobtitle}" size="35" maxlength="1024" />
   
   <h:outputText value="#{msg.user_location}:"/>
   <h:inputText id="location" value="#{WizardManager.bean.location}" disabled="#{WizardManager.bean.personPropertiesImmutability.location}" size="35" maxlength="1024" />
   
   <h:outputText value="#{msg.presence_provider}:" />
   <h:selectOneMenu value="#{WizardManager.bean.presenceProvider}" disabled="#{WizardManager.bean.personPropertiesImmutability.presenceProvider}">
		<f:selectItem itemValue="" itemLabel="(#{msg.none})"/>
		<f:selectItem itemValue="skype" itemLabel="Skype"/>
		<f:selectItem itemValue="yahoo" itemLabel="Yahoo"/>
	</h:selectOneMenu>
	
	<h:outputText value="#{msg.presence_username}:"/>
	<h:inputText value="#{WizardManager.bean.presenceUsername}" disabled="#{WizardManager.bean.personPropertiesImmutability.presenceUsername}" size="35" maxlength="256" />
	
	<h:outputText value="#{msg.sizeQuota}:" rendered="#{UsersBeanProperties.usagesEnabled == true}"/>
	<h:panelGroup>
   	<h:inputText value="#{WizardManager.bean.sizeQuota}" size="10" maxlength="256" rendered="#{UsersBeanProperties.usagesEnabled == true}"/>
   	<h:selectOneMenu value="#{WizardManager.bean.sizeQuotaUnits}" rendered="#{UsersBeanProperties.usagesEnabled == true}">
   	    <f:selectItem itemValue="gigabyte" itemLabel="#{msg.gigabyte}"/>
   		<f:selectItem itemValue="megabyte" itemLabel="#{msg.megabyte}"/>
   		<f:selectItem itemValue="kilobyte" itemLabel="#{msg.kilobyte}"/>
   	</h:selectOneMenu>
	</h:panelGroup>
</h:panelGrid>
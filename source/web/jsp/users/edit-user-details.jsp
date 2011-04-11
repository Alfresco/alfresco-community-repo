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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<script>

addEventToElement(window, 'load', pageLoaded, false);

function pageLoaded()
{
	document.getElementById("dialog:dialog-body:first-name").focus();
	updateButtonState();
}

function updateButtonState()
{
	if (document.getElementById("dialog:dialog-body:first-name").value.length == 0 ||
		document.getElementById("dialog:dialog-body:last-name").value.length == 0 ||
		document.getElementById("dialog:dialog-body:email").value.length == 0)
	{
		document.getElementById("dialog:finish-button").disabled = true;
	}
	else
	{
		document.getElementById("dialog:finish-button").disabled = false;
	}
}
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.person_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="2" cellspacing="2" width="100%" columnClasses=",alignTop,">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.first_name}:"/>
   <h:inputText id="first-name" value="#{DialogManager.bean.firstName}" disabled="#{UsersBeanProperties.immutability.firstName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.last_name}:"/>
   <h:inputText id="last-name" value="#{DialogManager.bean.lastName}" disabled="#{UsersBeanProperties.immutability.lastName}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.email}"/>
   <h:inputText id="email" value="#{DialogManager.bean.email}" disabled="#{UsersBeanProperties.immutability.email}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.user_organization}:"/>
   <h:inputText id="organisation" value="#{DialogManager.bean.personProperties.organization}" disabled="#{UsersBeanProperties.immutability.organization}" size="35" maxlength="1024" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.user_jobtitle}:"/>
   <h:inputText id="jobtitle" value="#{DialogManager.bean.personProperties.jobtitle}" disabled="#{UsersBeanProperties.immutability.jobtitle}" size="35" maxlength="1024" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.user_location}:"/>
   <h:inputText id="location" value="#{DialogManager.bean.personProperties.location}" disabled="#{UsersBeanProperties.immutability.location}" size="35" maxlength="1024" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.presence_provider}:"/>
   <h:selectOneMenu value="#{DialogManager.bean.personProperties.presenceProvider}" disabled="#{UsersBeanProperties.immutability.presenceProvider}">
		<f:selectItem itemValue="" itemLabel="(#{msg.none})"/>
		<f:selectItem itemValue="skype" itemLabel="Skype"/>
		<f:selectItem itemValue="yahoo" itemLabel="Yahoo"/>
	</h:selectOneMenu>
	
	<f:verbatim/>
	<h:outputText value="#{msg.presence_username}:"/>
	<h:inputText value="#{DialogManager.bean.personProperties.presenceUsername}" disabled="#{UsersBeanProperties.immutability.presenceUsername}" size="35" maxlength="256" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.user_description}:"/>
   <h:inputTextarea id="biography" value="#{DialogManager.bean.personDescription}" disabled="#{UsersBeanProperties.immutability.personDescription}" rows="6" cols="60" />
   
   <f:verbatim/>
   <h:outputText value="#{msg.user_avatar}:"/>
   <r:ajaxFileSelector id="avatar"
         value="#{DialogManager.bean.personPhotoRef}"
         label="#{msg.select_avatar_prompt}"
         initialSelection="#{DialogManager.bean.personProperties.homeFolder}"
         mimetypes="image/gif,image/jpeg,image/png"
         styleClass="selector" />
   
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="2" cellspacing="0" width="100%" style="padding-top:8px">
   <!-- custom properties for cm:person type -->
   <f:verbatim/>
   <r:propertySheetGrid id="person-props" value="#{DialogManager.bean.person}"
         var="personProps" columns="1" externalConfig="true" />
</h:panelGrid>
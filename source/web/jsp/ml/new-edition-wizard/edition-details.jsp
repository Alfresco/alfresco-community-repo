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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.edition_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" columns="1">
   <h:outputText value="#{msg.edition_notes}" />
   <h:inputTextarea value="#{WizardManager.bean.editionNotes}" rows="2" cols="50"/>
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" columns="2">
   <h:selectBooleanCheckbox id="add_translation" value="#{WizardManager.bean.minorChange}" immediate="false"/>
   <h:outputText value="#{msg.minor_change}" />
</h:panelGrid>



<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.other_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" columns="2">
   <h:selectBooleanCheckbox id="other_props" value="#{WizardManager.bean.otherProperties}" immediate="false"/>
   <h:outputText value="#{msg.modify_translation_properties}" />
</h:panelGrid>

<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

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

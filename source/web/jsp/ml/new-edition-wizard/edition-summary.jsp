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

<h:panelGrid columns="1" styleClass="workflowSelection">
   <h:panelGrid columns="1">
         <h:outputText value="#{msg.create_new_edition_using}" style="font-weight:bold;"/>
   </h:panelGrid>
    <h:panelGrid columns="2" style="padding-left: 10px;">
       <h:outputText value="#{msg.translation_title}:" />
       <h:outputText value="#{WizardManager.bean.title}" style="font-style:italic;"/>
       <h:outputText value="#{msg.translation_language}:" />
       <h:outputText value="#{WizardManager.bean.language}" style="font-style:italic;"/>
       <h:outputText value="#{msg.translation_author}:" />
       <h:outputText value="#{WizardManager.bean.author}" style="font-style:italic;"/>
    </h:panelGrid>
</h:panelGrid>

<h:outputText id="padding1" styleClass="paddingRow" value="&nbsp;" escape="false" />
<h:outputText id="padding2" styleClass="paddingRow" value="&nbsp;" escape="false" />

<h:panelGrid columns="1" styleClass="workflowSelection">
   <h:panelGrid columns="1">
      <h:outputText value="#{msg.new_edition_details}" style="font-weight:bold;"/>
   </h:panelGrid>
    <h:panelGrid columns="2" style="padding-left: 10px;">
       <h:outputText value="#{msg.version}:"/>
       <h:outputText value="#{WizardManager.bean.versionLabel}" style="font-style:italic;"/>
       <h:outputText value="#{msg.version_notes}:"/>
       <h:outputText value="#{WizardManager.bean.editionNotes}" style="font-style:italic;"/>
     </h:panelGrid>
</h:panelGrid>


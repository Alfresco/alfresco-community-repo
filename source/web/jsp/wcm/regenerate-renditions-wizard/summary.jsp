<!--
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
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
  -->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
          xmlns:a="urn:jsptld:/WEB-INF/alfresco.tld"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html">

  <jsp:directive.page language="java" buffer="32kb" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">

    <a:richList id="renditions-list" viewMode="details" value="#{WizardManager.bean.regeneratedRenditions}" var="r"
                binding="#{WizardManager.bean.regeneratedRenditionsRichList}"
                styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
                altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
                initialSortColumn="name" initialSortDescending="true"
                rendered="#{not empty WizardManager.bean.regeneratedRenditions}">
      <a:column id="col1" primary="true" width="200" style="padding: 2px; text-align:left">
        <f:facet name="header">
          <a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
        </f:facet>
        <f:facet name="small-icon">
          <a:actionLink id="col1-act1" value="#{r.name}" href="#{r.url}" target="new" image="#{r.fileTypeImage}" 
                        showLink="false" styleClass="inlineAction" />
        </f:facet>
        <a:actionLink id="col1-act2" value="#{r.name}" href="#{r.url}" target="new" />
      </a:column>
      <a:column id="col2" style="padding:2px;text-align:left">
        <f:facet name="header">
          <a:sortLink id="col2-sort" label="#{msg.description}" value="description" styleClass="header"/>
        </f:facet>
        <h:outputText id="col2-txt" value="#{r.description}" />
      </a:column>
      <a:column id="col6" actions="true" style="padding:2px;text-align:left">
        <f:facet name="header">
          <h:outputText id="col6-txt" value="#{msg.actions}"/>
        </f:facet>
        <a:actionLink href="#{r.url}" showLink="true" image="/images/icons/preview_website.gif" value="#{msg.file_preview}"/>
      </a:column>
      <a:dataPager styleClass="pager" />
    </a:richList>
  </h:panelGrid>
</jsp:root>

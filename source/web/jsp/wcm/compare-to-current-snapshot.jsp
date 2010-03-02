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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>

<h:dataTable value="#{DialogManager.bean.comparedNodes}" var="row" width="100%" styleClass="selectedItems" headerClass="selectedItemsHeader" cellspacing="0"
   cellpadding="4" style="margin-bottom: 4px;">
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msg.snapshot_name}" />
      </f:facet>
      <h:outputText value="#{row.name}" />
   </h:column>

   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msg.snapshot_path}" />
      </f:facet>
      <h:outputText value="#{row.path}" />
   </h:column>

   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msg.snapshot_status}" />
      </f:facet>
      <h:outputText value="#{row.status}" />
   </h:column>
</h:dataTable>

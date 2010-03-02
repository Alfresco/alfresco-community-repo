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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
             width="100%" rowClasses="mainSubText">
  <h:outputFormat value="#{msg.edit_file_prompt}">
    <f:param value="#{DialogManager.bean.avmNode.name}" />
  </h:outputFormat>
</h:panelGrid>
<h:panelGrid columns="1" cellpadding="2" style="padding:10px; vertical-align: middle"
             width="100%">
  <%-- downloadable file link --%>
  <a:actionLink styleClass="title" 
                image="#{DialogManager.bean.fileType32}" 
                value="#{DialogManager.bean.avmNode.name}" 
                href="#{DialogManager.bean.url}" />
</h:panelGrid>
<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
             width="100%" rowClasses="mainSubText">
  <h:outputText value="#{msg.edit_download_complete}" />
  
  <a:actionLink value="#{msg.edit_using_web_form}" showLink="true" action="dialog:close:dialog:promptForWebForm"/>
</h:panelGrid>

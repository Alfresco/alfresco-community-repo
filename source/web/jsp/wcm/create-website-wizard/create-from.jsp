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

<h:panelGrid columns="1">
   <h:selectOneRadio id="create-from" value="#{WizardManager.bean.createFrom}" layout="pageDirection">
      <f:selectItem itemValue="empty" itemLabel="#{msg.website_create_empty}" />
      <f:selectItem itemValue="existing" itemLabel="#{msg.website_create_existing}:" />
   </h:selectOneRadio>
   <h:panelGroup id="grp-1">
      <f:verbatim><div style="height:180px;*height:184px;margin-left:28px;width:90%;overflow-x:none;overflow-y:auto" class='selectListTable'></f:verbatim>
      <a:selectList id="website-list" value="#{WizardManager.bean.sourceWebProject}" style="width:100%" itemStyleClass="selectListItem">
         <a:listItems id="website-items" value="#{WizardManager.bean.webProjectsList}" cacheValue="false" />
      </a:selectList>
      <f:verbatim></div></f:verbatim>
      <f:verbatim><div style="margin-left:28px;margin-top:4px"></f:verbatim>
      <h:outputText id="txt-note" value="#{msg.website_sourcenote}" />
      <f:verbatim><br></f:verbatim>
      <a:actionLink id="lnk-showall" value="#{msg.website_sourceshowall}" actionListener="#{WizardManager.bean.toggleWebProjectsList}" rendered="#{!WizardManager.bean.showAllSourceProjects}" />
      <a:actionLink id="lnk-showtemplates" value="#{msg.website_sourceshowtemplates}" actionListener="#{WizardManager.bean.toggleWebProjectsList}" rendered="#{WizardManager.bean.showAllSourceProjects}" />
      <f:verbatim></div></f:verbatim>
   </h:panelGroup>
</h:panelGrid>
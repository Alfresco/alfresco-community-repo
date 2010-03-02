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

<h:panelGrid id="admin-panel" columns="1" cellpadding="6" cellspacing="6" border="0" width="100%">
   
   <a:actionLink value="#{msg.manage_users}" image="/images/icons/users.gif" action="dialog:manageUsers" styleClass="title" rendered="#{NavigationBean.allowUserGroupAdmin}" />
   
   <a:actionLink value="#{msg.manage_groups}" image="/images/icons/group.gif" padding="2" action="dialog:manageGroups" styleClass="title" rendered="#{NavigationBean.allowUserGroupAdmin}" />
   
   <a:actionLink value="#{msg.category_management}" image="/images/icons/categories.gif" padding="2" action="dialog:manageCategories" actionListener="#{CategoriesDialog.resetCategoryNavigation}" styleClass="title" />
   
   <a:actionLink value="#{msg.import}" image="/images/icons/import.gif" padding="2" action="dialog:import" actionListener="#{BrowseBean.setupSpaceAction}" styleClass="title">
      <f:param name="id" value="#{NavigationBean.currentNodeId}" />
   </a:actionLink>
   
   <a:actionLink value="#{msg.export}" image="/images/icons/export.gif" padding="2" action="dialog:export" actionListener="#{BrowseBean.setupSpaceAction}" styleClass="title">
      <f:param name="id" value="#{NavigationBean.currentNodeId}" />
   </a:actionLink>
   
   <a:actionLink value="#{msg.system_info}" image="/images/icons/info_icon.gif" padding="2" action="dialog:showSystemInfo" styleClass="title" />
   
   <a:actionLink value="#{msg.node_browser}" image="/images/icons/node_browser.gif" padding="2" action="dialog:showNodeBrowser" styleClass="title" />
</h:panelGrid>
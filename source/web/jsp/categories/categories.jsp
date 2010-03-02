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

      <h:outputText value="<div style='padding-left: 8px; padding-top: 4px; padding-bottom: 4px'>" escape="false" />
      <a:breadcrumb value="#{DialogManager.bean.location}" styleClass="title" />
      <h:outputText value="</div><div style='padding: 4px'>" escape="false" />
      <a:panel id="categories-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.items}">

         <a:richList id="categories-list"  viewMode="#{DialogManager.bean.viewMode}" pageSize="15" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
            value="#{DialogManager.bean.categories}" binding="#{DialogManager.bean.categoriesRichList}" var="r" initialSortColumn="name" initialSortDescending="true">

            <%-- Primary column for icons view mode --%>
            <a:column primary="true" style="padding:2px;text-align:left;vertical-align:top;">
               <f:facet name="header">
                  <a:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header" />
               </f:facet>
               <f:facet name="large-icon">
                  <a:actionLink value="#{r.name}" image="/images/icons/category.gif" actionListener="#{DialogManager.bean.clickCategory}" showLink="false">
                     <f:param name="id" value="#{r.id}" />
                  </a:actionLink>
               </f:facet>
               <f:facet name="small-icon">
                  <a:actionLink value="#{r.name}" image="/images/icons/category_small.gif" actionListener="#{DialogManager.bean.clickCategory}" showLink="false">
                     <f:param name="id" value="#{r.id}" />
                  </a:actionLink>
               </f:facet>
               <a:actionLink value="#{r.name}" actionListener="#{DialogManager.bean.clickCategory}" styleClass="header">
                  <f:param name="id" value="#{r.id}" />
               </a:actionLink>
            </a:column>

            <%-- Actions column --%>
            <a:column actions="true" style="text-align:left">
               <f:facet name="header">
                  <h:outputText value="#{msg.actions}" />
               </f:facet>
               <r:actions id="inline-category-actions" value="category_inline_actions" context="#{r}" showLink="false" styleClass="inlineAction" />
            </a:column>

            <a:dataPager styleClass="pager" />
         </a:richList>

      </a:panel>
<h:outputText value="</div>" escape="false" />
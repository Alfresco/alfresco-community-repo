
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
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>


<%-- load a bundle of properties with I18N strings --%>
<r:loadBundle var="msg" />

<a:panel id="users-panel" border="white" bgcolor="white"
   titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white"
   styleClass="mainSubTitle" label="#{msg.permissions}">

   <a:richList id="users-list"
      binding="#{DialogManager.bean.usersRichList}" viewMode="details"
      pageSize="10" styleClass="recordSet"
      headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
      altRowStyleClass="recordSetRowAlt" width="100%"
      value="#{DialogManager.bean.users}" var="r"
      initialSortColumn="userName" initialSortDescending="true">
   
      <%-- Primary column with full name --%>
      <a:column primary="true" width="200"
                style="padding:2px;text-align:left">
         <f:facet name="header">
            <a:sortLink label="#{msg.name}" value="fullName"
                        mode="case-insensitive" styleClass="header" />
         </f:facet>
         <f:facet name="small-icon">
            <h:graphicImage url="#{r.icon}" />
         </f:facet>
         <h:outputText value="#{r.fullName}" />
      </a:column>
   
      <%-- Username column --%>
      <a:column width="120" style="text-align:left">
         <f:facet name="header">
            <a:sortLink label="#{msg.authority}" value="userName"
                        styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.userName}" />
      </a:column>
      <%-- Created Date column for details view mode --%>

       <%-- Column to show whether the rule is local --%>
      <a:column style="text-align:left" id="column3">
         <f:facet name="header">
            <a:sortLink id="sortLink2" label="#{msg.inherited}" value="local" styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.inherited}">
            <a:convertBoolean />
         </h:outputText>
      </a:column>
      
      <%-- Permission column --%>
      <a:column style="text-align:left">
         <f:facet name="header">
            <a:sortLink label="#{msg.permissions}" value="perms"
                        styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.perms}" />
      </a:column>

      <%-- Actions column --%>
      <a:column actions="true" style="text-align:left">
         <f:facet name="header">
            <h:outputText value="#{msg.actions}" />
         </f:facet>
      
         <a:booleanEvaluator value="#{!r.inherited}">
         <a:actionLink value="#{msg.change_permissions}"
                       rendered="#{DialogManager.bean.rendered}"
                       image="/images/icons/edituser.gif" showLink="false"
                       action="dialog:editPermissions"
                       actionListener="#{EditPermissionsDialog.setupAction}">
            <f:param name="userName" value="#{r.userName}" />
         </a:actionLink>
         <a:actionLink value="#{msg.remove}"
                       rendered="#{DialogManager.bean.rendered}"
                       image="/images/icons/delete_person.gif" showLink="false"
                       action="dialog:removePermissions"
                       actionListener="#{RemovePermissionsDialog.setupAction}">
            <f:param name="userName" value="#{r.userName}" />
         </a:actionLink>
         </a:booleanEvaluator>
      </a:column>

      <a:dataPager styleClass="pager" />
   </a:richList>
</a:panel>

<f:verbatim>
   <table cellspacing="2" cellpadding="0" border="0" width="100%">
      <tr>
         <td>
         </f:verbatim>
            <h:selectBooleanCheckbox id="inherit-parent-box" 
            onclick="document.forms['dialog'].submit();  return true;"
            valueChangeListener="#{DialogManager.bean.inheritPermissionsValueChanged}"
            value="#{DialogManager.bean.inheritParenSpacePermissions}"/>
         <f:verbatim>   
         </td>
         <td width=100%>
            &nbsp;</f:verbatim><h:outputText id="apply-rec-msg" value="Inherit parent space permissions"/><f:verbatim>
         </td>
      </tr>
   </table>
</f:verbatim>

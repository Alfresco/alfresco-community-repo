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

<f:verbatim>
<script type="text/javascript">

   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:search-text").focus();
      updateButtonState();
   }
   
   function updateButtonState()
   {
      if (document.getElementById("dialog:dialog-body:search-text").value.length == 0)
      {
         document.getElementById("dialog:dialog-body:search-btn").disabled = true;
      }
      else
      {
         document.getElementById("dialog:dialog-body:search-btn").disabled = false;
      }
   }
</script>


<table cellspacing="2" cellpadding="2" border="0" width="100%">
   <tr>
      <td width="100%" valign="top"></f:verbatim>
      
      <%-- Users List --%> 
      <a:panel id="users-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.users}">
         <f:verbatim>
         <%
         PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");
         %>
         <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
               <td valign=top style="padding-top: 2px" width=20></f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16" /><f:verbatim></td>
               <td class="mainSubText"></f:verbatim><h:outputText value="#{msg.user_search_info}" /><f:verbatim></td>
            </tr>
         </table>
         <%
         PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");
         %>
         </f:verbatim>
         <f:verbatim><div style="padding: 6px;"></div></f:verbatim>
         <h:inputText id="search-text" value="#{UsersBeanProperties.searchCriteria}" size="35" maxlength="1024" onkeyup="updateButtonState();" onchange="updateButtonState();" />&nbsp;
                              <h:commandButton id="search-btn" value="#{msg.search}" action="#{DialogManager.bean.search}" disabled="true" />&nbsp;
                              <h:commandButton value="#{msg.show_all}" action="#{DialogManager.bean.showAll}" />
         <f:verbatim><div style="padding: 6px;"></div></f:verbatim>

         <a:richList id="users-list" binding="#{UsersBeanProperties.usersRichList}" viewMode="details" pageSize="10" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" value="#{DialogManager.bean.users}" var="r"
            initialSortColumn="userName" initialSortDescending="true">

            <%-- Primary column with full name --%>
            <a:column primary="true" width="200" style="padding:2px;text-align:left">
               <f:facet name="header">
                  <a:sortLink label="#{msg.name}" value="fullName" mode="case-insensitive" styleClass="header" />
               </f:facet>
               <f:facet name="small-icon">
                  <h:graphicImage url="/images/icons/person.gif" />
               </f:facet>
               <h:outputText value="#{r.fullName}" />
            </a:column>

            <%-- Username column --%>
            <a:column width="120" style="text-align:left">
               <f:facet name="header">
                  <a:sortLink label="#{msg.username}" value="userName" styleClass="header" />
               </f:facet>
               <h:outputText value="#{r.userName}" />
            </a:column>

            <%-- Home Space Path column --%>
            <a:column style="text-align:left">
               <f:facet name="header">
                  <h:outputText value="#{msg.homespace}" />
               </f:facet>
               <r:nodePath value="#{r.homeSpace}" disabled="true" showLeaf="true" />
            </a:column>

            <%-- Usage column --%>
            <a:column style="text-align:left">
               <f:facet name="header">
                  <h:outputText value="#{msg.sizeCurrent}" rendered="#{UsersBeanProperties.usagesEnabled == true}"/>
               </f:facet>
               <h:outputText value="#{r.sizeLatest}" rendered="#{UsersBeanProperties.usagesEnabled == true}">
                  <a:convertSize />
               </h:outputText>
            </a:column>
                                 
            <%-- Quota column --%>
            <a:column style="text-align:left">
               <f:facet name="header">
                  <h:outputText value="#{msg.sizeQuota}" rendered="#{UsersBeanProperties.usagesEnabled == true}"/>
               </f:facet>
               <h:outputText value="#{r.quota}" rendered="#{UsersBeanProperties.usagesEnabled == true}">
                  <a:convertSize />
               </h:outputText>
            </a:column>

            <%-- Actions column --%>
            <a:column actions="true" style="text-align:left">
               <f:facet name="header">
                  <h:outputText value="#{msg.actions}" />
               </f:facet>
               <a:actionLink value="#{msg.modify}" image="/images/icons/edituser.gif" showLink="false" action="wizard:editUser" actionListener="#{DialogManager.bean.setupUserAction}">
                  <f:param name="id" value="#{r.id}" />
               </a:actionLink>
               <a:actionLink rendered="#{r.isMutable}" value="#{msg.change_password}" image="/images/icons/change_password.gif" showLink="false" action="dialog:changePassword" actionListener="#{DialogManager.bean.setupUserAction}">
                  <f:param name="id" value="#{r.id}" />
               </a:actionLink>
               <a:booleanEvaluator value="#{r.userName != 'admin'}">
                  <a:actionLink value="#{msg.delete}" image="/images/icons/delete_person.gif" showLink="false" action="dialog:deleteUser" actionListener="#{DeleteUserDialog.setupUserAction}">
                     <f:param name="id" value="#{r.id}" />
                  </a:actionLink>
               </a:booleanEvaluator>
            </a:column>

            <a:dataPager styleClass="pager" />
         </a:richList>
         
         <a:panel id="quota-panel" rendered="#{UsersDialog.usersSize != 0 && UsersBeanProperties.usagesEnabled == true}">
         <f:verbatim>
            <table border="0" cellspacing="2" cellpadding="2">
               <tr>
                  <td></f:verbatim><h:outputText value="#{msg.quota_totalusage}:" /><f:verbatim></td>
                  <td></f:verbatim><h:outputText value="#{UsersDialog.usersTotalUsage}"><a:convertSize/></h:outputText><f:verbatim></td>
               </tr>
               <tr>
                  <td></f:verbatim><h:outputText value="#{msg.quota_totalquota}:" /><f:verbatim></td>
                  <td></f:verbatim><h:outputText value="#{UsersDialog.usersTotalQuota}"><a:convertSize/></h:outputText><f:verbatim></td>
               </tr>
            </table>
            </f:verbatim>
         </a:panel>

      </a:panel><f:verbatim>
      </td>
   </tr>
</table></f:verbatim>
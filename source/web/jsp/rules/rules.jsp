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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<h:panelGroup rendered="#{DialogManager.bean.ignoreInheritedRules}" id="panelGroup0">
   <f:verbatim>
      <%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
      <table id="table1">
         <tr>
            <td></f:verbatim> <h:graphicImage id="graphic0" url="/images/icons/info_icon.gif"/> <f:verbatim></td>
            <td></f:verbatim> <h:outputText value="#{msg.inherited_rules_being_ignored}"/> <f:verbatim></td>
         </tr>
      </table>
      <%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
   </f:verbatim>
</h:panelGroup>
<f:verbatim>
   <div style="padding: 4px"></div>
</f:verbatim>
<a:panel id="rules-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.rules}">
   <a:richList id="rulesList" viewMode="details" value="#{DialogManager.bean.rules}" var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
      initialSortColumn="createdDate" initialSortDescending="false" binding="#{DialogManager.bean.richList}">

      <%-- Primary column for details view mode --%>
      <a:column primary="true" width="200" style="padding:2px;text-align:left"  id="column1">
         <f:facet name="header">
            <a:sortLink id="sortLink0" label="#{msg.title}" value="title" mode="case-insensitive" styleClass="header" />
         </f:facet>
         <f:facet name="small-icon">
            <h:panelGroup id="panelGroup1">
               <a:booleanEvaluator id="bool_eval0" value="#{r.local}">
                  <a:actionLink id="link1" value="#{r.title}" image="/images/icons/rule.gif" actionListener="#{DialogManager.bean.setupRuleAction}" action="wizard:editRule" showLink="false">
                     <f:param name="id" value="#{r.id}" />
                  </a:actionLink>
               </a:booleanEvaluator>
               <a:booleanEvaluator id="bool_eval1" value="#{r.local == false}">
                  <h:graphicImage id="graphic1" value="/images/icons/rule.gif" title="#{r.title}" style="vertical-align: middle" />
               </a:booleanEvaluator>
            </h:panelGroup>
         </f:facet>
         <a:booleanEvaluator id="bool_eval2" value="#{r.local}">
            <a:actionLink id="link2" value="#{r.title}" actionListener="#{DialogManager.bean.setupRuleAction}" action="wizard:editRule">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </a:booleanEvaluator>
         <a:booleanEvaluator id="bool_eval3" value="#{r.local == false}">
            <h:outputText value="#{r.title}" />
         </a:booleanEvaluator>
      </a:column>

      <%-- Description column --%>
      <a:column style="text-align:left" id="column2">
         <f:facet name="header">
            <a:sortLink id="sortLink1" label="#{msg.description}" value="description" styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.description}" />
      </a:column>

      <%-- Column to show whether the rule is local --%>
      <a:column style="text-align:left" id="column3">
         <f:facet name="header">
            <a:sortLink id="sortLink2" label="#{msg.local}" value="local" styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.local}">
            <a:convertBoolean />
         </h:outputText>
      </a:column>

      <%-- Created Date column for details view mode --%>
      <a:column style="text-align:left" id="column4">
         <f:facet name="header">
            <a:sortLink id="sortLink3" label="#{msg.created_date}" value="createdDate" styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.createdDate}">
            <a:convertXMLDate type="both" dateStyle="long" timeStyle="short" />
         </h:outputText>
      </a:column>

      <%-- Modified Date column for details/icons view modes --%>
      <a:column style="text-align:left" id="column5">
         <f:facet name="header">
            <a:sortLink id="sortLink4" label="#{msg.modified_date}" value="modifiedDate" styleClass="header" />
         </f:facet>
         <h:outputText value="#{r.modifiedDate}">
            <a:convertXMLDate type="both" dateStyle="long" timeStyle="short" />
         </h:outputText>
      </a:column>

      <%-- Rule status collumn --%>
      <a:column style="text-align:left" id="column6">
         <f:facet name="header">
            <h:outputText value="#{msg.rule_active}" styleClass="header" />
         </f:facet>
         <h:outputText value="#{!r.rule.ruleDisabled}">
            <a:convertBoolean />
         </h:outputText>
      </a:column>

      <%-- Actions column --%>
      <a:column actions="true" style="text-align:left" id="column7">
         <f:facet name="header">
            <h:outputText value="#{msg.actions}" />
         </f:facet>
         <a:booleanEvaluator id="bool_eval4" value="#{r.local}">
            <a:actionLink id="link3" value="#{msg.delete}" image="/images/icons/delete.gif" showLink="false" styleClass="inlineAction" actionListener="#{DialogManager.setupParameters}" action="dialog:deleteRule">
               <f:param name="nodeRef" value="#{r.rule.nodeRef}" />
            </a:actionLink>
            <a:actionLink id="link4" value="#{msg.change_details}" image="/images/icons/change_rule.gif" showLink="false" styleClass="inlineAction" actionListener="#{DialogManager.bean.setupRuleAction}" action="wizard:editRule">
               <f:param name="id" value="#{r.id}" />
            </a:actionLink>
         </a:booleanEvaluator>
      </a:column>

      <a:dataPager id="dataPager1" styleClass="pager" />
   </a:richList>

</a:panel>
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
<%@ page isELIgnored="false" %>

   <script type="text/javascript">
      addEventToElement(window, 'load', pageLoaded, false);
      function pageLoaded() { document.getElementById("wizard:finish-button").focus(); }
   </script>

   <!-- General properties -->
   <h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
               width="100%" rowClasses="wizardSectionHeading">
      <h:outputText value="&#160;#{msg.general_properties}" escape="false" />
   </h:panelGrid>

   <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
      <a:selectList id="webproject-list" multiSelect="false" activeSelect="true" style="width:100%;" 
                    itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;"
                    escapeItemLabel="false" escapeItemDescription="false">
         <a:listItem value="${WizardManager.bean.name}"
                     image="/images/icons/website_large.gif"
                     label="${WizardManager.bean.websiteLabelAttribute}"
                     description="${WizardManager.bean.websiteDescriptionAttribute}" /> 
      </a:selectList>
   </h:panelGrid>

   <%-- Web Content Forms --%>
   <h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;" width="100%" rowClasses="wizardSectionHeading">
      <h:outputText value="&#160;#{msg.website_web_content_forms}" escape="false" />
   </h:panelGrid>

   <h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" width="100%">
      <h:outputText rendered="#{empty WizardManager.bean.forms}" value="#{msg.no_selected_items}"/>
      <a:selectList id="form-list" multiSelect="false" activeSelect="true" style="width:100%;" 
                    itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;"
                    escapeItemLabel="false" escapeItemDescription="false">
         <c:forEach items="${WizardManager.bean.forms}" var="form">
            <a:listItem value="${form.name}"
                        image="/images/icons/webform_large.gif"
                        label="${form.formLabelAttribute}"
                        description="${form.formDescriptionAttribute}" />
         </c:forEach>
      </a:selectList>
   </h:panelGrid>

   <%-- Selected Workflows --%>
   <h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;" width="100%" rowClasses="wizardSectionHeading">
      <h:outputText value="&#160;#{msg.website_selected_workflows}" escape="false" />
   </h:panelGrid>

   <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
      <h:outputText rendered="#{empty WizardManager.bean.workflows}" value="#{msg.no_selected_items}"/>
      <a:selectList id="workflow-list" multiSelect="false" activeSelect="true" style="width:100%;" 
                    itemStyle="vertical-align: top; margin-right: 5px; padding-right: 5px;"
                    escapeItemLabel="false" escapeItemDescription="false">
         <c:forEach items="${WizardManager.bean.workflows}" var="workflow">
            <a:listItem value="${workflow.name}"
                        image="/images/icons/workflow_large.gif"
                        label="${workflow.workflowLabelAttribute}"
                        description="${workflow.workflowDescriptionAttribute}" />
         </c:forEach>
      </a:selectList>
   </h:panelGrid>

   <%-- Users and Roles --%>
   <h:panelGrid columns="1" cellpadding="2" style="padding-top:16px" width="100%" rowClasses="wizardSectionHeading">
      <h:outputText value="&#160;#{msg.create_website_summary_users}" escape="false" />
   </h:panelGrid>

  <h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%" style="margin-left:12px">
    <a:selectList id="users-list" 
                  multiSelect="false"
                  activeSelect="true" 
                  style="width:100%;"
                  itemStyle="vertical-align: top; margin-right: 5px;"
                  escapeItemLabel="false" escapeItemDescription="false">
		<c:forEach items="${WizardManager.bean.invitedUsers}" var="user">
	        <a:listItem value="${user.name}"
                    	image="/images/icons/user_large.gif"
          				label="${user.userLabelAttribute}"
          				description="${user.userDescriptionAttribute}" />
      </c:forEach>
    </a:selectList>
  </h:panelGrid>

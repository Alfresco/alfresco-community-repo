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
  <h:outputFormat value="#{msg.prompt_for_web_form_explanation}">
    <f:param value="#{DialogManager.bean.avmNode.name}"/>
   </h:outputFormat>
</h:panelGrid>
<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.web_form}:"/>
   <h:selectOneMenu value="#{DialogManager.bean.formName}" 
                    rendered="#{!empty DialogManager.bean.formChoices}">
      <f:selectItems value="#{DialogManager.bean.formChoices}" />
   </h:selectOneMenu>
   <h:outputText value="#{msg.sandbox_no_web_forms}" 
                 style="font-style: italic"
                 rendered="#{empty DialogManager.bean.formChoices}"/>
</h:panelGrid>
<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
             width="100%" rowClasses="mainSubText">
  <h:outputFormat value="#{msg.prompt_for_web_form_continue_msg}">
    <f:param value="#{DialogManager.bean.avmNode.name}"/>
  </h:outputFormat>
</h:panelGrid>
   

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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page isELIgnored="false" %>

<script type="text/javascript">
  window.onload = function() { document.getElementById("wizard:finish-button").focus(); }
</script>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <a:selectList id="webproject-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%;"
		itemStyle="vertical-align: top; margin-right: 5px;">
    <a:listItem label="<b>${WizardManager.bean.name}</b>"
                value="${WizardManager.bean.name}"
                image="/images/icons/website_large.gif">
      <jsp:attribute name="description">
	<table width="100%" cellspacing="0" cellpadding="0" border="0">
	  <colgroup><col width="25%"/><col width="75%"/></colgroup>
	  <tbody>
            <tr><td>${msg.website_dnsname}:</td><td> ${WizardManager.bean.dnsName}</td></tr>
            <tr><td>${msg.website_webapp}:</td><td> ${WizardManager.bean.webapp}</td></tr>
            <tr><td>${msg.title}:</td><td> ${WizardManager.bean.title}</td></tr>
            <tr><td>${msg.description}:</td>
	      <td>
		<c:choose>
		  <c:when test="${empty WizardManager.bean.description}">
		    <span style="font-style:italic">${msg.description_not_set}</span>
		  </c:when>
		  <c:otherwise>${WizardManager.bean.description}</c:otherwise>
		</c:choose>
	      </td>
	    </tr>
	  </tbody>
	</table>
      </jsp:attribute>
    </a:listItem>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.website_web_content_forms}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" width="100%">
  <h:outputText rendered="#{empty WizardManager.bean.forms}"
		value="#{msg.no_selected_items}"/>
  <a:selectList id="form-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%;"
		itemStyle="vertical-align: top; margin-right: 5px;">
    <c:forEach items="${WizardManager.bean.forms}" var="r">
      <a:listItem label="<b>${r.name}</b>"
                  value="${r.name}"
                  image="/images/icons/webform_large.gif">
	<jsp:attribute name="description">
	  <table width="100%" cellspacing="0" cellpadding="0" border="0">
	    <colgroup><col width="25%"/><col width="75%"/></colgroup>
	    <tbody>
              <tr><td>${msg.name}:</td><td> ${r.name}</td></tr>
              <tr><td>${msg.title}:</td><td> ${r.title}</td></tr>
              <tr><td>${msg.output_path_pattern}:</td><td> ${r.outputPathPattern}</td></tr>
              <tr><td>${msg.description}:</td>
		<td>
		  <c:choose>
		    <c:when test="${empty WizardManager.bean.description}">
		      <span style="font-style:italic">${msg.description_not_set}</span>
		    </c:when>
		    <c:otherwise>${r.description}</c:otherwise>
		  </c:choose>
		</td>
              <tr><td>${msg.workflow}:</td>
		<td>
		  <c:choose>
		    <c:when test="${r.workflow == null}">
		      <span style="font-style:italic">${msg.none}</span>
		    </c:when>
		    <c:otherwise>${r.workflow.title}</c:otherwise>
		  </c:choose>
		</td>
	      </tr>
	    </tbody>
	  </table>
	</jsp:attribute>
      </a:listItem>
    </c:forEach>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px;padding-bottom:4px;"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.website_selected_workflows}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" width="100%">
  <h:outputText rendered="#{empty WizardManager.bean.workflows}"
		value="#{msg.no_selected_items}"/>
  <a:selectList id="workflow-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%;"
		itemStyle="vertical-align: top; margin-right: 5px;">
    <c:forEach items="${WizardManager.bean.workflows}" var="r">
      <a:listItem label="<b>${r.title}</b>"
                  value="${r.name}"
                  image="/images/icons/workflow_large.gif">
	<jsp:attribute name="description">
	  <table width="100%" cellspacing="0" cellpadding="0" border="0">
	    <colgroup><col width="25%"/><col width="75%"/></colgroup>
	    <tbody>
              <tr><td>${msg.description}:</td>
		<td>
		  <c:choose>
		    <c:when test="${empty r.description}">
		      <span style="font-style:italic">${msg.description_not_set}</span>
		    </c:when>
		    <c:otherwise>${r.description}</c:otherwise>
		  </c:choose>
		</td>
	      </tr>
              <tr><td>${msg.website_filename_pattern}:</td><td> ${r.filenamePattern}</td></tr>
	    </tbody>
	  </table>
	</jsp:attribute>
      </a:listItem>
    </c:forEach>
  </a:selectList>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top:16px"
      width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.create_website_summary_users}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" 
	     width="100%"
	     style="margin-left:12px">
  <a:selectList id="users-list" 
                multiSelect="false"
                activeSelect="true" 
                style="width:100%;"
		itemStyle="vertical-align: top; margin-right: 5px;">
    <c:forEach items="${WizardManager.bean.invitedUsers}" var="r">
      <a:listItem label="<b>${r.name}</b>"
                  value="${r.name}"
                  image="/images/icons/user_large.gif">
	<jsp:attribute name="description">
	  <table width="100%" cellspacing="0" cellpadding="0" border="0">
	    <colgroup><col width="25%"/><col width="75%"/></colgroup>
	    <tbody>
              <tr><td>${msg.roles}:</td><td> ${r.role}</td></tr>
	    </tbody>
	  </table>
	</jsp:attribute>
      </a:listItem>
    </c:forEach>
  </a:selectList>
</h:panelGrid>

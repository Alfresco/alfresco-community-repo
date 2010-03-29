<!--
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
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
-->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:a="urn:jsptld:/WEB-INF/alfresco.tld"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" buffer="32kb" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <script type="text/javascript">
    function apply_default_workflow_changed(value)
    {
      value = String(value) == 'true';
      document.getElementById("wizard:wizard-body:sdw-pg-2").style.display = value ? "block" : "none";
    }
  </script>
  <h:panelGroup id="sdw-pg-1">
    <h:outputText id="sdw-question" 
		  value="#{msg.create_form_select_default_workflow_apply_default_workflow}" 
		  escape="false" />

    <h:selectOneRadio id="sdw-apply-default-workflow-select-one"
		      required="false"
		      onclick="apply_default_workflow_changed(this.value)"
		      value="#{WizardManager.bean.applyDefaultWorkflow}">
      <f:selectItem itemLabel="#{msg.yes}" itemValue="#{true}"/>
      <f:selectItem itemLabel="#{msg.no_not_now}" itemValue="#{false}"/>
    </h:selectOneRadio>

    <f:verbatim><div style="margin-top:10px">&#160;</div></f:verbatim>
    <h:panelGroup id="sdw-pg-2" style="#{WizardManager.bean.applyDefaultWorkflow?'display:block':'display:none'}">
      <h:outputText id="sdw-select-workflow" 
  		    value="#{msg.create_form_select_default_workflow_select_workflow}:" 
  		    escape="false" />
      <h:panelGroup id="workflow-list-div"
  	            style="margin:5px 0px;height:144px;*height:148px;width:100%;overflow:auto;display:block;" 
                    styleClass="selectListTable">
        <a:selectList id="workflow-list" 
  		      multiSelect="false" 
  		      style="width:100%" 
  		      itemStyleClass="selectListItem"
  		      value="#{WizardManager.bean.defaultWorkflowName}">
          <a:listItems value="#{WizardManager.bean.defaultWorkflowChoices}" />
        </a:selectList>
      </h:panelGroup>
    </h:panelGroup>
  </h:panelGroup>
</jsp:root>

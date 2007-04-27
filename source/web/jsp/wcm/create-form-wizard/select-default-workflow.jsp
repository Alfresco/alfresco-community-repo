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
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
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
		    onchange="apply_default_workflow_changed(this.value)"
		    value="#{WizardManager.bean.applyDefaultWorkflow}">
    <f:selectItem itemLabel="#{msg.yes}" itemValue="#{true}"/>
    <f:selectItem itemLabel="#{msg.no_not_now}" itemValue="#{false}"/>
  </h:selectOneRadio>

  <f:verbatim><div style="margin-top:10px">&nbsp;</div></f:verbatim>
  <h:panelGroup id="sdw-pg-2" style="#{WizardManager.bean.applyDefaultWorkflow?'display:block':'display:none'}">
    <h:outputText id="sdw-select-workflow" 
  		value="#{msg.create_form_select_default_workflow_select_workflow}:" 
  		escape="false" />
    <f:verbatim><div id="workflow-list-div"
  		     style="margin:5px 0px;height:144px;*height:148px;width:100%;overflow:auto" class="selectListTable"></f:verbatim>
    <a:selectList id="workflow-list" 
  		  multiSelect="false" 
  		  style="width:100%" 
  		  itemStyleClass="selectListItem"
  		  value="#{WizardManager.bean.defaultWorkflowName}">
      <a:listItems value="#{WizardManager.bean.defaultWorkflowChoices}" />
    </a:selectList>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>
</h:panelGroup>

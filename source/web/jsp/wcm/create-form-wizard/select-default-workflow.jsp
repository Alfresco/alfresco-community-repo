<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<script type="text/javascript">
function apply_default_workflow_changed(value)
{
  document.getElementById("wizard:wizard-body:sdw-pg-2").style.display = value == 'true' ? "block" : "none";
}
</script>
<h:panelGroup id="sdw-pg-1">
  <h:outputText id="sdw-question" 
		value="#{msg.create_form_select_default_workflow_apply_default_workflow}" 
		escape="false" />
  <h:selectOneRadio id="sdw-apply-default-workflow-yes"
		    required="false"
		    onchange="apply_default_workflow_changed(this.value)"
		    value="#{WizardManager.bean.applyDefaultWorkflow}">
    <f:selectItem id="sdw-apply-default-workflow-yes-item"
		  itemLabel="#{msg.yes}" itemValue="true"/>
    <f:selectItem id="sdw-apply-defalt-workflow-no-item"
		  itemLabel="#{msg.no_not_now}" itemValue="false" value="false"/>
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

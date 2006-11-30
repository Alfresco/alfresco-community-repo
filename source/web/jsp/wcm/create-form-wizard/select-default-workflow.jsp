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

<h:panelGroup>
   <f:verbatim><div style="height:108px;*height:112px;width:300px;overflow:auto" class="selectListTable"></f:verbatim>
   <a:selectList id="workflow-list" multiSelect="false" style="width:276px" itemStyleClass="selectListItem"
		 value="#{WizardManager.bean.defaultWorkflowName}">
      <a:listItems value="#{WizardManager.bean.defaultWorkflowChoices}" />
   </a:selectList>
   <f:verbatim></div></f:verbatim>
</h:panelGroup>

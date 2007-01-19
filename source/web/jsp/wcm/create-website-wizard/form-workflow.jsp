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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid id="grid-1" columns="1" cellpadding="2" cellpadding="2" width="100%">
   <r:propertySheetGrid id="task-props" value="#{DialogManager.bean.workflowMetadataNode}" 
         var="taskProps" columns="1" externalConfig="true" />
</h:panelGrid>

<h:panelGroup id="grp-1" rendered="#{DialogManager.bean.filenamePattern != null}">
   <h:panelGrid id="grid-2" columns="1" cellpadding="2" style="padding-top:4px;padding-bottom:4px;"
         width="100%" rowClasses="wizardSectionHeading">
      <h:outputText id="msg-1" value="&nbsp;#{msg.workflow_settings}" escape="false" />
   </h:panelGrid>
   
   <h:panelGrid id="grid-3" columns="2" cellpadding="2" cellspacing="2" style="margin-left:16px">
      <h:outputText id="msg-2" value="&nbsp;#{msg.website_filename_match}" escape="false" />
      <h:inputText id="in-1" value="#{DialogManager.bean.filenamePattern}" size="70" />
   </h:panelGrid>
</h:panelGroup>

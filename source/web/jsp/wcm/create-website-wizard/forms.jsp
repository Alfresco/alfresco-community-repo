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

<h:panelGrid columns="1" cellpadding="2" cellpadding="3" width="100%">
   <h:outputText styleClass="mainSubText" value="#{msg.website_select_form}:" />
   <a:selectList multiSelect="true" activeSelect="false" value="#{WizardManager.bean.formsSelectedValue}"
         styleClass="selectListTable" itemStyleClass="selectListItem">
      <a:listItems value="#{WizardManager.bean.formsList}" />
   </a:selectList>
   
   <h:outputText styleClass="mainSubText" value="#{msg.website_select_form}:" />
   <a:selectList multiSelect="false" activeSelect="false"
         styleClass="selectListTable" itemStyleClass="selectListItem">
      <a:listItems value="#{WizardManager.bean.formsList}" />
   </a:selectList>
   
   <h:outputText styleClass="mainSubText" value="#{msg.website_select_form}:" />
   <a:selectList var="r" multiSelect="false" activeSelect="true"
         styleClass="selectListTable" itemStyleClass="selectListItem">
      <a:listItems value="#{WizardManager.bean.formsList}" />
      <h:commandButton value="Add to List" styleClass="dialogControls">
         <f:param name="id" value="#{r.value}" />
      </h:commandButton>
   </a:selectList>
</h:panelGrid>

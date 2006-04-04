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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<h:panelGrid columns="1">
   <h:outputText value="#{msg.how_to_create_space}"/>
   <h:selectOneRadio id="create-from" value="#{WizardManager.bean.createFrom}" layout="pageDirection">
      <f:selectItem itemValue="scratch" itemLabel="#{msg.from_scratch}" />
      <f:selectItem itemValue="existing" itemLabel="#{msg.based_on_existing_space}" />
      <f:selectItem itemValue="template" itemLabel="#{msg.using_a_template}" />
   </h:selectOneRadio>
</h:panelGrid>
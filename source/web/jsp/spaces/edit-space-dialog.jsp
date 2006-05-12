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

<h:panelGrid columns="1" rowClasses="wizardSectionHeading, paddingRow" 
             cellpadding="2" cellspacing="2" width="100%">
   <h:outputText value="#{msg.space_props}" />
   <r:propertySheetGrid id="space-props" value="#{DialogManager.bean.editableNode}" 
                        var="spaceProps" columns="1" labelStyleClass="propertiesLabel" 
                        externalConfig="true" cellpadding="2" cellspacing="2" />
</h:panelGrid>
    

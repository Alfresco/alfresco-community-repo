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

<r:page>

<f:view>
   
   <h2>Property sheet with standard JSF components</h2>
   
   <h:form acceptCharset="UTF-8" id="propertySheetForm">
   
      <r:propertySheetGrid value="/gav.doc">
         <h:outputText value='#{MockDDService.types[node.type].propertiesMap["name"].displayName}: ' />
         <h:inputText value="#{node.name}" />
         <h:outputText value='#{MockDDService.types[node.type].propertiesMap["description"].displayName}: ' />
         <h:inputText value="#{node.description}" />         
         <h:outputText value='#{MockDDService.types[node.type].propertiesMap["created"].displayName}: ' />
         <h:inputText value="#{node.created}" disabled='#{MockDDService.types[node.type].propertiesMap["created"].readOnly}'>
            <f:convertDateTime dateStyle="short" pattern="d/MM/yyyy" />
         </h:inputText>
         <h:outputText value='#{MockDDService.types[node.type].propertiesMap["modified"].displayName}: ' />
         <h:inputText value="#{node.modified}">
            <f:convertDateTime dateStyle="short" pattern="d/MM/yyyy" />
         </h:inputText>
         <!-- TODO: Put the keywords in here to test the custom converter tag -->
      </r:propertySheetGrid>
   
      <div style="color:red;"><h:messages layout="table" /></div>
      <br/>
      <h:commandButton value="Update Properties" action="#{node.persist}"/>  
   
   </h:form>

   <br/><hr/>
   
   <h2>Property sheet with custom property components</h2>
   
   <h:form acceptCharset="UTF-8" id="propertySheetForm2">
   
      <r:propertySheetGrid value="/kev.txt" var="node2">
         <r:property value="name" columns="1" />
         <r:property value="description" columns="1" />
         <r:property value="created" columns="1" />
         <r:property value="modified" columns="2" />
         <r:property value="non-existent" columns="1" />
      </r:propertySheetGrid>

      <div style="color:red;"><h:messages layout="table" /></div>
      <br/>
      <h:commandButton value="Update Properties" action="#{node2.persist}"/>

      <p>
      
      <h:commandButton id="show-zoo-page" value="Show Zoo" action="showZoo" />

   </h:form>
      
</f:view>

</r:page>
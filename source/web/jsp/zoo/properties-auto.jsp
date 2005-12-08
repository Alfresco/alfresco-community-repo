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
   
   <h2>Auto configured property sheet</h2>
   
   <h:form acceptCharset="UTF-8" id="propertySheetForm3">
   
      <r:propertySheetGrid value="/sop.txt" var="node3">
      </r:propertySheetGrid>

      <div style="color:red;"><h:messages layout="table" /></div>
      <br/>
      <h:commandButton value="Update Properties" action="#{node3.persist}"/>
   
   </h:form>
   
   <br/><hr/>
   
   <h2>Config driven property sheet (WEB-INF/web-client-config.xml)</h2>
   
   <h:form acceptCharset="UTF-8" id="propertySheetForm4">
   
      <r:propertySheetGrid value="/sop.txt" var="node4" externalConfig="true">
      </r:propertySheetGrid>
   
      <div style="color:red;"><h:messages layout="table" /></div>
      <br/>
      <h:commandButton value="Update Properties" action="#{node4.persist}"/>

      <p>
      
      <h:commandButton id="show-zoo-page" value="Show Zoo" action="showZoo" />
   </h:form>
      
   
</f:view>

</r:page>
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

<f:view>
   <%-- load a bundle of properties I18N strings here --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="userForm" >
   
      <h3>New User details:</h3>
      
      <%@ include file="userform.jsp" %>
      
      <p>
      
      <h:commandButton id="submit" value="OK" action="success" actionListener="#{UserListBean.addUserOK}" />
      <%-- Use of the 'immediate' attribute forces the cancel action impl to execute
           in the Apply Request Values processing phase - it would normally be deferred to
           the Invoke Application phase. This means is fires before the form validation
           occurs - allow pure UI events to either navigate or change the UI without
           seeing validation errors etc. --%>
      <h:commandButton id="cancel" value="Cancel" action="cancel" immediate="true" />
      
   </h:form>
</f:view>

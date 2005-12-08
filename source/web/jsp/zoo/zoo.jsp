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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<r:page>

<f:view>
   
   <h2>The Zoo</h2>
   
   <h:form acceptCharset="UTF-8" id="zooForm">
      
      <h:commandButton id="show-components-zoo" value="Component Zoo" action="showRichListZoo" />
      <br/><br/>
      <%--
      <h:commandButton id="show-property-zoo" value="Property Zoo" action="showPropertyZoo" />
      <br/><br/>
      <h:commandButton id="show-auto-property-zoo" value="Auto Property Zoo" action="showAutoPropertyZoo" />
      <br/><br/>
      --%>
      <h:commandButton id="show-image-picker" value="Image Picker Zoo" action="showImagePickerZoo" />
      <br/><br/>
      <h:commandButton id="dyna-desc" value="Dynamic Description Zoo" action="showDynaDescZoo" />
      <br/><br/>
      <h:commandButton id="show-user-list" value="UserList Test Pages" action="showUserlist" />
      
      <p/><p/>
      <hr/><p/>
      <h:commandButton id="show-web-client" value="Back to the Web Client" action="showWebClient" />
      
   </h:form>
   
</f:view>

</r:page>
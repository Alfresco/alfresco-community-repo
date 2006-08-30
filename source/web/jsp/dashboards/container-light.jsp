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
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_my_alfresco">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

   <h:form acceptCharset="UTF-8" id="dashboard">
   
   <table cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
         <td width="100%" valign="top">
            <f:subview id="dash-body">
   				<jsp:include page="<%=Application.getDashboardManager().getLayoutPage()%>" />
   			</f:subview>
         </td>
      </tr>
      <tr>
         <td style="padding-left:8px"><a:actionLink value="#{msg.configure}" image="/images/icons/configure_dashboard.gif" padding="2" action="wizard:configureDashboard" /></td>
      </tr>
   </table>
   
   </h:form>
    
</f:view>

</r:page>
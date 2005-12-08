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
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_admin_store_browser">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%@ include file="admin-title.jsp" %>
   
   <h:commandLink action="#{AdminNodeBrowseBean.selectStores}">
       <h:outputText styleClass="mainSubText" value="Refresh view"/>
   </h:commandLink>
   
   <br>
   <br>
   <h:outputText styleClass="mainTitle" value="Stores"/>
   <br>
   
   <h:dataTable id="stores" border="1" value="#{AdminNodeBrowseBean.stores}" var="store">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Reference"/>
           </f:facet>
           <h:commandLink action="#{AdminNodeBrowseBean.selectStore}">
               <h:outputText value="#{store}"/>
           </h:commandLink>
       </h:column>
   </h:dataTable>

   <br>
</f:view>

</r:page>

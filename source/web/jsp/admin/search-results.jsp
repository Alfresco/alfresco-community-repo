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

<r:page titleId="title_admin_search_results">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%@ include file="admin-title.jsp" %>
   
   <table>
      <tr>
      <td>   
         <h:commandLink action="#{AdminNodeBrowseBean.selectStores}">
	        <h:outputText value="Stores"/>
	     </h:commandLink>
	  </td>
	  <td>&nbsp;|&nbsp;</td>
	  <td>
         <h:commandLink action="nodeBrowser">
            <h:outputText value="Node Browser"/>
         </h:commandLink>
	  </td>
   </table>
   
   <br>
   <h:outputText styleClass="mainTitle" value="Search"/>

   <table>
      <tr>
         <td><b>Search Language:</b></td><td><h:outputText value="#{AdminNodeBrowseBean.queryLanguage}"/></td>
      </tr>
      <tr>
         <td><b>Search:</b></td><td><h:outputText value="#{AdminNodeBrowseBean.query}"/></td>
      </tr>
   </table>

   <br>
   <span class="mainTitle">Results (<h:outputText value="#{AdminNodeBrowseBean.searchResults.length}"/> rows)</span>

   <h:dataTable id="searchResults" border="1" value="#{AdminNodeBrowseBean.searchResults.rows}" var="row">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Name"/>
           </f:facet>
           <h:commandLink action="#{AdminNodeBrowseBean.selectResultNode}">
           	   <h:outputText value="#{row.QName}"/>
           </h:commandLink>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Node"/>
           </f:facet>
           <h:outputText value="#{row.childRef}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Parent"/>
           </f:facet>
           <h:commandLink action="#{AdminNodeBrowseBean.selectResultNode}">
               <h:outputText value="#{row.parentRef}"/>
           </h:commandLink>
       </h:column>
   </h:dataTable>

   <br>
</f:view>

</r:page>

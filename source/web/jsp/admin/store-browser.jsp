<%--
  Copyright (C) 2005 Alfresco, Inc.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

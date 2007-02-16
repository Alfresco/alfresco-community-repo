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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_admin_node_browser">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>

   <%@ include file="admin-title.jsp" %>

   <h:commandLink id="selectStores" action="#{AdminNodeBrowseBean.selectStores}">
       <h:outputText styleClass="mainSubText" value="Stores"/>
   </h:commandLink>
   <br>
   <br>

   <h:outputText styleClass="mainTitle" value="Search"/>

   <h:form id="searchForm">
      <table>
         <tr>
            <td>
               <h:selectOneMenu id="queryLanguage" value="#{AdminNodeBrowseBean.queryLanguage}">
                   <f:selectItems value="#{AdminNodeBrowseBean.queryLanguages}"/>
               </h:selectOneMenu>
            </td>
            <td>
               <h:inputText id="query" size="100" value="#{AdminNodeBrowseBean.query}"/>
            </td>
            <td>
               <h:commandButton id="submitSearch" action="#{AdminNodeBrowseBean.submitSearch}" value="Search"/>
            </td>
         </tr>
         <tr>
            <td></td>
            <td>
                <h:message styleClass="errorMessage" id="queryError" for="query"/>
            </td>
            <td></td>
         </tr>
      </table>
   </h:form>
   
   <h:outputText styleClass="mainTitle" value="Node Identifier"/>

   <table>
   <tr>
      <td><nobr><b>Primary Path:</b></nobr></td><td>
          <nobr>
          <h:commandLink id="selectPrimaryPath" action="#{AdminNodeBrowseBean.selectPrimaryPath}">
              <h:outputText id="primaryPath" value="#{AdminNodeBrowseBean.primaryPath}"/>
          </h:commandLink>
          </nobr>
      </td>
   </tr>
   <tr>
      <td><b>Reference:</b></td><td><h:outputText id="nodeRef" value="#{AdminNodeBrowseBean.nodeRef}"/></td>
   </tr>
   <tr>
      <td><b>Type:</b></td><td><h:outputText id="nodeType" value="#{AdminNodeBrowseBean.nodeType}"/></td>
   </tr>
   <tr>
      <td><b>Parent:</b></td>
      <td>
          <h:commandLink id="selectPrimaryParent" action="#{AdminNodeBrowseBean.selectPrimaryParent}">
              <h:outputText id="primaryParent" value="#{AdminNodeBrowseBean.primaryParent}"/>
          </h:commandLink>
      </td>
   </tr>
   </table>

   <br>
   <h:outputText styleClass="mainTitle" value="Properties"/>
   
   <h:dataTable id="properties" border="1" value="#{AdminNodeBrowseBean.properties}" var="property">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Name"/>
           </f:facet>
           <h:outputText value="#{property.name}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Value"/>
           </f:facet>
           <h:outputText styleClass="errorMessage" value="--collection--" rendered="#{property.collection == true}"/>
           <h:dataTable id="values" border="0" cellspacing="0" value="#{property.values}" var="value" rendered="#{property.values.rowCount > 0}">
                <h:column>
                    <h:outputLink value="#{value.url}" target="_blank" rendered="#{value.content}">
                        <h:outputText value="#{value.value}" />
                    </h:outputLink>
                    <h:commandLink id="selectNodeProperty" action="#{AdminNodeBrowseBean.selectNodeProperty}" rendered="#{value.nodeRef}">
                        <h:outputText value="#{value.value}"/>
                    </h:commandLink>
                    <h:outputText value="#{value.value}" rendered="#{value.content == false && value.nodeRef == false && value.nullValue == false}"/>
                    <h:outputText styleClass="errorMessage" value="--null--" rendered="#{value.nullValue}"/>
               </h:column>
           </h:dataTable>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Property Type"/>
           </f:facet>
           <h:outputText value="#{property.dataType}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Value Type"/>
           </f:facet>
           <h:dataTable id="valueTypes" border="0" cellspacing="0" value="#{property.values}" var="value">
                <h:column>
                    <h:outputText value="#{value.dataType}" rendered="#{property.dataType == null || property.any}"/>
                </h:column>
           </h:dataTable>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Residual"/>
           </f:facet>
           <h:outputText value="#{property.residual}"/>
       </h:column>
   </h:dataTable>

   <br>
   <h:outputText styleClass="mainTitle" value="Aspects"/>

   <h:dataTable id="aspects" value="#{AdminNodeBrowseBean.aspects}" var="aspect">
       <h:column>
           <h:outputText value="#{aspect}"/>
       </h:column>
   </h:dataTable>

   <br>
   <h:outputText styleClass="mainTitle" value="Permissions"/>
   
   <table>
   <tr>
      <td><b>Inherit:</b></td><td><h:outputText id="inheritPermissions" value="#{AdminNodeBrowseBean.inheritPermissions}"/></td>
   </tr>
   </table>
   
   <br>
   <h:dataTable id="permissions" border="1" value="#{AdminNodeBrowseBean.permissions}" var="permission">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Assigned Permission"/>
           </f:facet>
           <h:outputText value="#{permission.permission}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="To Authority"/>
           </f:facet>
           <h:outputText value="#{permission.authority}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Access"/>
           </f:facet>
           <h:outputText value="#{permission.accessStatus}"/>
       </h:column>
   </h:dataTable>

   <br>
   <h:outputText styleClass="mainTitle" value="Children"/>

   <h:dataTable id="children" border="1" value="#{AdminNodeBrowseBean.children}" var="child">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Child Name"/>
           </f:facet>
           <h:outputText value="#{child.QName}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Child Node"/>
           </f:facet>
           <h:commandLink id="selectChild" action="#{AdminNodeBrowseBean.selectChild}">
               <h:outputText value="#{child.childRef}"/>
           </h:commandLink>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Primary"/>
           </f:facet>
           <h:outputText value="#{child.primary}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Association Type"/>
           </f:facet>
           <h:outputText value="#{child.typeQName}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Index"/>
           </f:facet>
           <h:outputText value="#{child.nthSibling}"/>
       </h:column>
   </h:dataTable>

   <br>
   <h:outputText styleClass="mainTitle" value="Associations"/>

   <h:dataTable id="assocs" border="1" value="#{AdminNodeBrowseBean.assocs}" var="assoc">
       <h:column>
           <f:facet name="header">
               <h:outputText value="To Node"/>
           </f:facet>
           <h:commandLink id="selectToNode" action="#{AdminNodeBrowseBean.selectToNode}">
               <h:outputText value="#{assoc.targetRef}"/>
           </h:commandLink>
           
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Association Type"/>
           </f:facet>
           <h:outputText value="#{assoc.typeQName}"/>
       </h:column>
   </h:dataTable>

   <br>
   <h:outputText styleClass="mainTitle" value="Parents"/>

   <h:dataTable id="parents" border="1" value="#{AdminNodeBrowseBean.parents}" var="parent">
       <h:column>
           <f:facet name="header">
               <h:outputText value="Child Name"/>
           </f:facet>
           <h:outputText value="#{parent.QName}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Parent Node"/>
           </f:facet>
           <h:commandLink id="selectParent" action="#{AdminNodeBrowseBean.selectParent}">
               <h:outputText value="#{parent.parentRef}"/>
           </h:commandLink>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Primary"/>
           </f:facet>
           <h:outputText value="#{parent.primary}"/>
       </h:column>
       <h:column>
           <f:facet name="header">
               <h:outputText value="Association Type"/>
           </f:facet>
           <h:outputText value="#{parent.typeQName}"/>
       </h:column>
   </h:dataTable>

   <br>
</f:view>

</r:page>

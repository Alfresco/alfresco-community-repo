<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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

<%
   boolean closeWindow = false;
   String param = request.getParameter("close");
   if (param != null && param.equalsIgnoreCase("true"))
   {
      closeWindow = true;
   }
%>

<script type="text/javascript">
   var isClose = <%=closeWindow%>;
</script>

<r:page title="<%=Application.getDialogManager().getTitle() %>">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="dialog">
      <table cellspacing="0" cellpadding="3" border="0" width="100%">
         <tr>
            <td width="100%" valign="top">
            
               <a:errors message="#{DialogManager.errorMessage}" styleClass="errorMessage" />
               
               <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
               <f:subview id="dialog-body">
               	<jsp:include page="<%=Application.getDialogManager().getPage() %>" />
               </f:subview>
               <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
            </td>
            
            <td valign="top">
               <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
               <r:dialogButtons id="dialog-buttons" styleClass="wizardButton" />
               <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
            </td>
         </tr>
      </table>
   </h:form>

<script type="text/javascript">
   window.addEvent('domready', init);

   function init()
   {
      if (isClose == true)
      {
         document.getElementById("dialog:cancel-button").onclick = function()
         {
            if(navigator.appName == "Microsoft Internet Explorer")
            {
               window.opener = this;
            }
            window.open('', '_parent', '');
            window.close();
         }
      }
   }

</script>
    
</f:view>

</r:page>
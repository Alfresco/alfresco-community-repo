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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
   <script type="text/javascript">
      window.addEvent('load', pageLoaded);
      
      function pageLoaded()
      {
         spaceSelectorDisable();
      }
      
      function spaceSelectorDisable()
      {
         var checked0 = document.forms['dialog']['dialog:dialog-body:radio-space'][0].checked;
         if (checked0)
         {
            document.getElementById("spaceSelector").style.visibility="hidden";
         }
         else
         {
            document.getElementById("spaceSelector").style.visibility="visible";
         }
      }
   </script>
   
   <table cellspacing="0" cellpadding="3" border="0" width="100%">
      <tr>
         <td width="100%" valign="top">
            <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
            <td valign=top style="padding-top:2px" width=20></f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/><f:verbatim></td>
            <td class="mainSubText">
            </f:verbatim><h:outputFormat value="#{msg.copy_work_with}">
            <f:param value="#{CCProperties.document.name}" />
            </h:outputFormat><f:verbatim>
            <br>
            </f:verbatim><h:outputText value="#{msg.copy_checkin_changes}" /><f:verbatim>
            </td>
            </tr>
            </table>
            <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
         </td>
      </tr>
      <tr>
         <td width="100%" valign="top">
            <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
            <table cellpadding="2" cellspacing="2" border="0">
               <tr>
                  <td class="mainSubText"></f:verbatim><h:outputText value="#{msg.copy_store_prompt}" /><f:verbatim></td>
               </tr>
               <tr>
                  <td>
                     </f:verbatim><h:selectOneRadio id="radio-space" value="#{CCProperties.copyLocation}" layout="pageDirection" onclick="spaceSelectorDisable();">
                     <f:selectItem itemValue="current" itemLabel="#{msg.store_space_current}" />
                     <f:selectItem itemValue="other" itemLabel="#{msg.store_space_selected}:" />
                     </h:selectOneRadio><f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td style="padding-left:26px">
                     <%-- Space selector to allow user to pick a Space --%>
                     </f:verbatim>
                     <r:ajaxFolderSelector id="spaceSelector" label="#{msg.select_space_prompt}" 
                                           value="#{CCProperties.selectedSpaceId}" 
                                           initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
                                           styleClass="selector" />
                     <f:verbatim>
                  </td>
               </tr>
            </table>
            <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
         </td>
      </tr>
   </table>
</f:verbatim>

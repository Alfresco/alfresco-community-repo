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

<h:panelGrid columns="1" cellpadding="3" cellspacing="0" border="0" width="100%">
   <h:outputText value="#{msg.select_layout}" />
   
   <a:imagePickerRadioPanel id="layout-type" columns="4" spacing="4" value="#{WizardManager.bean.layout}"
                       onclick="javascript:itemSelected(this);" panelBorder="greyround" panelBgcolor="#F5F5F5">
      <a:listItems value="#{WizardManager.bean.layoutIcons}" />
   </a:imagePickerRadioPanel>
   
   <h:panelGroup>
      <f:verbatim>
      <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
      <table border='0'>
         <tr>
            <td valign='top'>
               </f:verbatim>
               <h:graphicImage id="info-icon" url="/images/icons/info_icon.gif" />
               <f:verbatim>
            </td>
            <td valign='top' align='left'>
               </f:verbatim>
               <a:dynamicDescription selected="#{WizardManager.bean.layout}">
                  <a:descriptions value="#{WizardManager.bean.layoutDescriptions}" />
               </a:dynamicDescription>
               <f:verbatim>
            </td>
         </tr>
      </table>
      <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
      </f:verbatim>
   </h:panelGroup>
   
</h:panelGrid>

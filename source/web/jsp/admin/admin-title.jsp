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

<h:form id="node-browser-titlebar" style="margin-bottom: 8px;">

   <table width="100%">
      <tr>
         <td>
            <h:graphicImage value="/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
         </td>
         <td>
            <nobr><h:outputText styleClass="mainTitle" value="#{msg.title_admin_node_browser}"/></nobr>
         </td>
         <td width="100%" align="right">
            <h:commandButton value="#{msg.close}" action="dialog:close" />
         </td>
      </tr>
   </table>

</h:form>

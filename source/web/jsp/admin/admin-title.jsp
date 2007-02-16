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

<h:form id="node-browser-titlebar">

   <table width="100%">
      <tr>
         <td>
            <h:graphicImage value="/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
         </td>
         <td>
            <nobr><h:outputText styleClass="mainTitle" value="#{msg.title_admin_node_browser}"/></nobr>
         </td>
         <td width="100%" align="right">
            <h:commandButton value="#{msg.close}" action="adminConsole" />
         </td>
      </tr>
   </table>

</h:form>

<br>

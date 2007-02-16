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

<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="16kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.app.Application" %>

<body bgcolor="#ffffff" style="background-image: url(<%=request.getContextPath()%>/images/logo/AlfrescoFadedBG.png); background-repeat: no-repeat; background-attachment: fixed">

<r:page titleId="title_noaccess">

   
   <table width=100% height=90% align=center>
      <tr width=100% align=center>
         <td valign=middle align=center width=100%>
            
            <table cellspacing=0 cellpadding=0 border=0>
            <tr><td width=7><img src='<%=request.getContextPath()%>/images/parts/white_01.gif' width=7 height=7 alt=''></td>
            <td background='<%=request.getContextPath()%>/images/parts/white_02.gif'>
            <img src='<%=request.getContextPath()%>/images/parts/white_02.gif' width=7 height=7 alt=''></td>
            <td width=7><img src='<%=request.getContextPath()%>/images/parts/white_03.gif' width=7 height=7 alt=''></td>
            </tr>
            <tr><td background='<%=request.getContextPath()%>/images/parts/white_04.gif'>
            <img src='<%=request.getContextPath()%>/images/parts/white_04.gif' width=7 height=7 alt=''></td><td bgcolor='white'>
            
            <table border=0 cellspacing=4 cellpadding=2>
               <tr>
                  <td align=center>
                     <img src='<%=request.getContextPath()%>/images/logo/AlfrescoLogo200.png' width=200 height=58 alt="Alfresco" title="Alfresco">
                  </td>
               </tr>
               
               <tr>
                  <td align=center>
                     <span class='mainSubTitle'><%=Application.getMessage(session, "no_access")%></span>
                  </td>
               </tr>
               
            </table>
            
            </td><td background='<%=request.getContextPath()%>/images/parts/white_06.gif'>
            <img src='<%=request.getContextPath()%>/images/parts/white_06.gif' width=7 height=7 alt=''></td></tr>
            <tr><td width=7><img src='<%=request.getContextPath()%>/images/parts/white_07.gif' width=7 height=7 alt=''></td>
            <td background='<%=request.getContextPath()%>/images/parts/white_08.gif'>
            <img src='<%=request.getContextPath()%>/images/parts/white_08.gif' width=7 height=7 alt=''></td>
            <td width=7><img src='<%=request.getContextPath()%>/images/parts/white_09.gif' width=7 height=7 alt=''></td></tr>
            </table>
            
         </td>
      </tr>
   </table>

</r:page>

</body>
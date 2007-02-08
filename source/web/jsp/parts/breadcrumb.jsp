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
<%-- Breadcrumb area --%>
<%-- Designed to support a variable height breadcrumb --%>
<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_1.gif" width=4 height=7></td>
   <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_2.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_3.gif" width=4 height=7></td>
</tr>

<tr>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_4.gif)"></td>
   <td bgcolor="#dfe6ed">
      <%-- Breadcrumb component --%>
      <div style="padding-left:8px" class="headbarTitle">
         <a:breadcrumb value="#{NavigationBean.location}" styleClass="headbarLink" />
      </div>
   </td>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_6.gif)"></td>
</tr>

<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_7.gif" width=4 height=10></td>
   <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_8.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_9.gif" width=4 height=10></td>
</tr>

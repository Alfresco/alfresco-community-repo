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
<%-- Breadcrumb area --%>
<%-- Designed to support a variable height breadcrumb --%>
<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_1.gif" width="4" height="7" alt=""/></td>
   <td style="width:100%; background-image: url(<%=request.getContextPath()%>/images/parts/headbar_2.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_3.gif" width="4" height="7" alt=""/></td>
</tr>

<tr>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_4.gif)"></td>
   <td style="background-color: #dfe6ed;">
      <%-- Breadcrumb component --%>
      <div style="padding-left:8px" class="headbarTitle">
         <a:breadcrumb value="#{NavigationBean.location}" styleClass="headbarLink" />
      </div>
   </td>
   <td style="background-image: url(<%=request.getContextPath()%>/images/parts/headbar_6.gif)"></td>
</tr>

<tr>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_7.gif" width="4" height="10" alt=""/></td>
   <td style="width: 100%; background-image: url(<%=request.getContextPath()%>/images/parts/headbar_8.gif)"></td>
   <td><img src="<%=request.getContextPath()%>/images/parts/headbar_9.gif" width="4" height="10" alt=""/></td>
</tr>

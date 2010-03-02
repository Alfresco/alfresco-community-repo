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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.app.Application" %>

<r:page titleId="title_error">

<table cellspacing="0" cellpadding="2" width="100%">
   <tr>
      <%-- Top level toolbar and company logo area --%>
      <td width=100%>
         <table cellspacing="0" cellpadding="0" width="100%">
            <tr>
               <td><a href="${pageContext.request.contextPath}/faces/jsp/dialog/about.jsp"><img src="${pageContext.request.contextPath}/images/logo/AlfrescoLogo32.png" width=32 height=30 alt="<%=Application.getMessage(session, "title_about")%>" title="<%=Application.getMessage(session, "title_about")%>" border=0 style="padding-right:4px"></a></td>
               <td><img src="${pageContext.request.contextPath}/images/parts/titlebar_begin.gif" width="10" height="30"></td>
               <td width=100% style="background-image: url(${pageContext.request.contextPath}/images/parts/titlebar_bg.gif)">
                  <span class="topToolbarTitle"><%=Application.getMessage(session, "system_error")%></span>
               </td>
               <td><img src="${pageContext.request.contextPath}/images/parts/titlebar_end.gif" width="8" height="30"></td>
            </tr>
         </table>
      </td>
   </tr>
   <tr>
      <td>
         <r:systemError styleClass="errorMessage" detailsStyleClass="mainSubTextSmall" showDetails="false" />
      </td>
   </tr>
</table>

</r:page>

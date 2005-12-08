<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
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
               <td><a href="http://www.alfresco.org" target="new"><img src="<%=request.getContextPath()%>/images/logo/AlfrescoLogo32.png" width=32 height=30 alt="Alfresco" title="Alfresco" border=0 style="padding-right:4px"></a></td>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_begin.gif" width="10" height="30"></td>
               <td width=100% style="background-image: url(<%=request.getContextPath()%>/images/parts/titlebar_bg.gif)">
                  <span class="topToolbarTitle"><%=Application.getMessage(session, "system_error")%></span>
               </td>
               <td><img src="<%=request.getContextPath()%>/images/parts/titlebar_end.gif" width="8" height="30"></td>
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

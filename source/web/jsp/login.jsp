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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page import="javax.servlet.http.Cookie" %>

<%@ page buffer="16kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<%
   Cookie authCookie = AuthenticationHelper.getAuthCookie(request);
   
   // remove the username cookie value if explicit logout was requested by the user
   if (session.getAttribute(AuthenticationHelper.SESSION_INVALIDATED) != null)
   {
      if (authCookie != null)
      {
         authCookie.setMaxAge(0);
         response.addCookie(authCookie);
      }
   }
   else
   {      
      // setup value used by JSF bean state ready for login page if we find the cookie
      if (authCookie != null && authCookie.getValue() != null)
      {
         session.setAttribute(AuthenticationHelper.SESSION_USERNAME, authCookie.getValue());
      }
   }
%>

<body bgcolor="#ffffff" style="background-image: url(<%=request.getContextPath()%>/images/logo/AlfrescoFadedBG.png); background-repeat: no-repeat; background-attachment: fixed">

<r:page titleId="title_login">

<f:view>
   <%-- load a bundle of properties I18N strings here --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="loginForm" >
   
   <table width=100% height=98% align=center>
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
                  <td colspan=2>
                     <img src='<%=request.getContextPath()%>/images/logo/AlfrescoLogo200.png' width=200 height=58 alt="Alfresco" title="Alfresco">
                  </td>
               </tr>
               
               <tr>
                  <td colspan=2>
                     <span class='mainSubTitle'><h:outputText value="#{msg.login_details}" />:</span>
                  </td>
               </tr>
               
               <tr>
                  <td>
                     <h:outputText value="#{msg.username}"/>:
                  </td>
                  <td>
                     <%-- input text field, with an example of a nested validator tag --%>
                     <h:inputText id="user-name" value="#{LoginBean.username}" validator="#{LoginBean.validateUsername}" style="width:150px" />
                  </td>
               </tr>
               
               <tr>
                  <td>
                     <h:outputText value="#{msg.password}"/>:
                  </td>
                  <td>
                     <%-- password text field, with an example of a validation bean method --%>
                     <%-- the validation method adds a faces message to be displayed by a message tag --%>
                     <h:inputSecret id="user-password" value="#{LoginBean.password}" validator="#{LoginBean.validatePassword}" style="width:150px" />
                  </td>
               </tr>
               
               <tr>
                  <td>
                     <h:outputText value="#{msg.language}"/>:
                  </td>
                  <td>
                     <%-- language selection drop-down --%>
                     <h:selectOneMenu id="language" value="#{UserPreferencesBean.language}" style="width:150px" onchange="document.forms['loginForm'].submit(); return true;">
                        <f:selectItems value="#{UserPreferencesBean.languages}" />
                     </h:selectOneMenu>
                  </td>
               </tr>
               
               <tr>
                  <td colspan=2 align=right>
                     <h:commandButton id="submit" action="#{LoginBean.login}" value="#{msg.login}" />
                  </td>
               </tr>
               
               <tr>
                  <td colspan=2>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages style="padding-top:8px; color:red; font-size:10px" layout="table" />
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
            
            <div id="no-cookies" style="display:none">
               <table cellpadding="0" cellspacing="0" border="0" style="padding-top:16px;">
                  <tr>
                     <td>
                        <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                        <table cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                              <td class="mainSubText">
                                 <h:outputText value="#{msg.no_cookies}" />
                              </td>
                           </tr>
                        </table>
                        <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                     </td>
                  </tr>
               </table>
            </div>
            <script>
               document.cookie="_alfTest"
               var cookieEnabled = (document.cookie.indexOf("_alfTest") != -1);
               if (cookieEnabled == false)
               {
                  document.getElementById("no-cookies").style.display = 'inline';
               }
            </script>
            
         </td>
      </tr>
      
   </table>
      
   </h:form>
</f:view>

<script>

   if (document.getElementById("loginForm:user-name").value.length == 0)
   {
      document.getElementById("loginForm:user-name").focus();
   }
   else
   {
      document.getElementById("loginForm:user-password").focus();
   }

</script>

</r:page>

</body>
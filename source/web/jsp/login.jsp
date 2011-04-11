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

<%@ page import="org.alfresco.web.app.servlet.BaseServlet" %>
<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>
<%@ page import="org.alfresco.web.ui.common.Utils" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.bean.LoginBean" %>
<%@ page import="org.springframework.extensions.surf.util.I18NUtil" %>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="java.util.Locale" %>

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
      String authCookieValue;
      if (authCookie != null && (authCookieValue = AuthenticationHelper.getAuthCookieValue(authCookie)) != null)
      {
         session.setAttribute(AuthenticationHelper.SESSION_USERNAME, authCookieValue);
      }
   }

%>

<body bgcolor="#ffffff" style="background-image: url(<%=request.getContextPath()%>/images/logo/AlfrescoFadedBG.png); background-repeat: no-repeat; background-attachment: fixed">

<r:page titleId="title_login">

<f:view>
<%
   FacesContext fc = FacesContext.getCurrentInstance();

   // set locale for JSF framework usage (passed on by Localization Filter)
   fc.getViewRoot().setLocale(I18NUtil.getLocale());
   
   // set permissions error if applicable
   if (session.getAttribute(LoginBean.LOGIN_NOPERMISSIONS) != null)
   {
   	Utils.addErrorMessage(Application.getMessage(fc, LoginBean.MSG_ERROR_LOGIN_NOPERMISSIONS));
   	session.setAttribute(LoginBean.LOGIN_NOPERMISSIONS, null);
   }
%>

   <%-- load a bundle of properties I18N strings here --%>
   <r:loadBundle var="msg"/>

   <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

   <h:form acceptcharset="UTF-8" id="loginForm" >

   <%-- Propagate the redirect URL parameter --%>
   <h:inputHidden value="#{LoginOutcomeBean.redirectURL}" id="redirectURL"/>  

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

               <%-- warning message for Team mode --%>
               <h:outputText id="team-login-warning" rendered="#{NavigationBean.teamMode}" value="#{LoginBean.teamLoginWarningHTML}" escape="false" />
               
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
                     <h:inputText id="user-name" value="#{LoginBean.username}" validator="#{LoginBean.validateUsername}" onkeyup="updateButtonState();" onchange="updateButtonState();" style="width:150px" />
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
                     <h:outputText value="#{msg.language}:" rendered="#{LoginBean.languageSelect}" />
                  </td>
                  <td>
                     <%-- language selection drop-down --%>
                     <h:selectOneMenu id="language" value="#{UserPreferencesBean.language}" style="width:150px" onchange="document.forms['loginForm'].submit(); return true;" rendered="#{LoginBean.languageSelect}">
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
               document.cookie="_alfTest=_alfTest"
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

    function validate()
    {
        return validateUserNameForLogin(document.getElementById("loginForm:user-name"), null, false);
    }

    function updateButtonState()
    {
      document.getElementById("loginForm:submit").disabled = !validate();
    }

    document.getElementById("loginForm").onsubmit = validate;
    if (document.getElementById("loginForm:user-name").value.length == 0)
    {
       document.getElementById("loginForm:user-name").focus();
    }
    else
    {
       document.getElementById("loginForm:user-password").focus();
    }
    updateButtonState();

</script>

</r:page>

</body>
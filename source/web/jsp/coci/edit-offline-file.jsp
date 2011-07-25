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
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@taglib prefix="a" uri="/WEB-INF/alfresco.tld" %>
<%@taglib prefix="c" uri="/WEB-INF/c.tld" %>

<script type="text/javascript">
<!--
   var continueCountdown = document.getElementById("dialog:dialog-body:continueCountdown").value;

   
   window.onload = startTimer;	
   
   function startTimer()
   {
      timer(5);
   }
   
   function timer(second)
   {
      if (continueCountdown == "true")
      {
         document.getElementById("time").innerHTML = second;
         if (second > 0)
         {
            second--;
            window.setTimeout('timer('+second+')', 1000);
         }
         else
         {
            document.location.href = getContextPath() + document.getElementById("dialog:dialog-body:url").value;
         }
      }
   }
   
   function stopCountdown()
   {
      continueCountdown = false;
      document.location.href = getContextPath() + document.getElementById("dialog:dialog-body:url").value;
   }
   
//-->
</script>

<h:inputHidden value="#{DialogManager.bean.continueCountdown}" id="continueCountdown"/>   
<h:inputHidden value="#{CCProperties.document.properties.url}" id="url"/>
      
<h:panelGrid columns="4" cellpadding="1" cellspacing="1" rendered="#{DialogManager.bean.continueCountdown}">
   <h:outputText value="#{msg.offline_download}"/>
   <f:verbatim>&nbsp;<b id="time">5</b>&nbsp;</f:verbatim>	
   <a:actionLink 
          style="text-decoration: underline;"
          value="#{msg.offline_start_download}"
          onclick="javascript:stopCountdown();"/>
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="1" cellspacing="1" rendered="#{DialogManager.bean.continueCountdown == false}">
   <h:outputText value="#{msg.offline_download_not_started}"/>
   <a:actionLink 
          style="text-decoration: underline;"
          value="#{msg.click_here}"
          onclick="javascript:stopCountdown();"/>
</h:panelGrid>	

<f:verbatim>
   <div style="padding: 4px"></div>
</f:verbatim>

<h:panelGrid columns="2" cellpadding="1" cellspacing="1">
   <h:outputText value="#{msg.offline_download_auto_change}"/>		
   <a:actionLink value="#{msg.click_here}" 
          action="dialog:userConsole"
          actionListener="#{UsersDialog.setupUserAction}"
          style="text-decoration: underline;">
    <f:param name="id" value="#{NavigationBean.currentUser.person.id}" />
 </a:actionLink>
</h:panelGrid>    

<f:verbatim>
   <div style="padding: 4px"></div>
</f:verbatim>

<h:panelGrid columns="3" cellpadding="1" cellspacing="1">
   <h:graphicImage url="/images/icons/Help_icon.gif" alt="#{msg.help}"/>
   <h:outputText value="#{msg.offline_help}"/> 		
   <a:actionLink value="#{msg.click_here}"
                 style="text-decoration: underline;"
                 target="_blank"
                 href="#{NavigationBean.helpUrl}" />
</h:panelGrid>

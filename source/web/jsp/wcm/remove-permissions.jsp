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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:finish-button").onclick = showProgress;
   }
   
   function showProgress()
   {
      document.getElementById('progress').style.display = 'block';
   }
</script>

<div id="progress" style="margin-left: 90px; margin-top: 4px; margin-bottom: 4px; display: none">
   <img src="<%=request.getContextPath()%>/images/icons/process_animation.gif" width=174 height=14>
</div>
</f:verbatim>

<h:panelGrid id="remove-msg" columns="1" cellpadding="2" style="padding-top:2px; padding-bottom:2px;" width="100%">
	<h:outputText value="#{msg.remove_permissions}"/>
</h:panelGrid>

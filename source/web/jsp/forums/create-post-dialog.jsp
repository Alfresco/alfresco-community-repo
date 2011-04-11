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

<f:verbatim>
<script type="text/javascript">
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:message").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:message").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>
</f:verbatim>

<h:panelGrid cellpadding="2" cellspacing="2" border="0" width="100%"
             rowClasses="wizardSectionHeading, paddingRow">
   <h:outputText value="#{msg.message}" />
   <h:panelGrid cellpadding="2" cellspacing="6" border="0" columns="3" 
                columnClasses="alignTop, alignTop, alignTop">
      <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
      <h:outputText value="#{msg.message}:" />
      <h:inputTextarea id="message" value="#{DialogManager.bean.content}" rows="6" cols="70" 
                          onkeyup="checkButtonState();" onchange="checkButtonState();" />
   </h:panelGrid>
</h:panelGrid>
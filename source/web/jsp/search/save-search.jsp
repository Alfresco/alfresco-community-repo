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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
   var finishButtonPressed = false;
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:name").focus();
      document.getElementById("dialog:finish-button").onclick = function() {finishButtonPressed = true; clear_dialog();}
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:name").value.length == 0 )
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
   
   function validate()
   {
      if (finishButtonPressed)
      {
         finishButtonPressed = false;
         return validateName(document.getElementById("dialog:dialog-body:name"), 
                             unescape('</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" encodeForJavaScript="true" /><f:verbatim>'),
                             true);
      }
      else
      {
         return true;
      }
   }
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.search_props}" />
</h:panelGrid>

<h:panelGrid id="savesearch-panel" columns="3" cellpadding="2" cellspacing="2" border="0">
   <h:outputText value="#{msg.name}:" />
   <h:inputText id="name" value="#{SearchProperties.searchName}" size="35" maxlength="1024"
         onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();" />
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.description}:" />
   <h:inputText value="#{SearchProperties.searchDescription}" size="35" maxlength="1024" />
   <h:outputText value=""/>
   <h:outputText value=""/>
   <h:panelGroup>
      <h:selectBooleanCheckbox value="#{SearchProperties.searchSaveGlobal}" />
      <h:outputText style="vertical-align: 20%" value="#{msg.save_search_global}" />
   </h:panelGroup>
   <h:outputText value=""/>
</h:panelGrid>
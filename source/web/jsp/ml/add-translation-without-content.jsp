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

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">

   <h:outputText/>
   <h:outputText value="#{msg.title}:"/>
   <h:inputText id="title" value="#{DialogManager.bean.title}"  maxlength="1024" size="35" immediate="false"/>

   <h:outputText/>
   <h:outputText value="#{msg.description}:"/>
   <h:inputTextarea id="description" value="#{DialogManager.bean.description}" cols="35" rows="5" immediate="false"/>

   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.author}:"/>
   <h:inputText id="author" value="#{DialogManager.bean.author}"   maxlength="1024" size="35" immediate="false" onkeyup="checkButtonState();" onchange="checkButtonState();"/>

   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.language}:"/>
   <h:selectOneMenu id="language" value="#{DialogManager.bean.language}" immediate="false"  onchange="checkButtonState();" onkeydown="checkButtonState();" onkeyup="checkButtonState();">
      <f:selectItem  itemLabel="#{msg.select_language}" itemValue="null"/>
      <f:selectItems value="#{DialogManager.bean.unusedLanguages}"/>
   </h:selectOneMenu>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.other_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
	<h:selectBooleanCheckbox value="#{DialogManager.bean.showOtherProperties}" />
	<h:outputText id="text10" value="#{msg.modify_props_when_page_closes}" />
</h:panelGrid>


<script type="text/javascript">

      function checkButtonState()
      {
         if (document.getElementById("dialog:dialog-body:author").value.length == 0 ||
             document.getElementById("dialog:dialog-body:language").selectedIndex == 0 )
         {
            document.getElementById("dialog:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("dialog:finish-button").disabled = false;
         }
      }

</script>



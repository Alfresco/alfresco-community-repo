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
   <h:outputText value="#{msg.ml_common_content_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.author}:"/>
   <h:inputText id="author" value="#{DialogManager.bean.author}"  maxlength="1024" size="35" immediate="false" onkeyup="checkButtonState();" onchange="checkButtonState();" />
   
   <%-- language selection drop-down --%>   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.language}:"/>
   <h:selectOneMenu id="language" value="#{DialogManager.bean.language}" immediate="false" onchange="checkButtonState();" onkeydown="checkButtonState();" onkeyup="checkButtonState();">
      <f:selectItem itemLabel="#{msg.select_language}" itemValue=""/>
      <f:selectItems value="#{DialogManager.bean.filterLanguages}"/>
   </h:selectOneMenu>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" style="padding-top: 4px;" width="100%" rowClasses="wizardSectionHeading, paddingRow">
   <h:outputText value="#{msg.ml_other_options}" escape="false" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" columns="2">
   <h:selectBooleanCheckbox id="add_translation" value="#{DialogManager.bean.addTranslationAfter}" onchange="submit();" immediate="false"/>
   <h:outputText value="#{msg.ml_add_trans_when_diag_close}" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" id="panel_adding_mode" >
   <h:panelGrid style="padding-top: 4px;padding-left: 15px;">
      <h:selectOneRadio required="false" value="#{DialogManager.bean.addingMode}" disabled="#{!DialogManager.bean.addTranslationAfter}" immediate="false" id="radioWithContent" layout="pageDirection">
         <f:selectItem itemValue="ADD_WITH_CONTENT"    itemLabel="#{msg.ml_with_content}" itemDisabled="#{!DialogManager.bean.addTranslationAfter}"/>
         <f:selectItem itemValue="ADD_WITHOUT_CONTENT" itemLabel="#{msg.ml_just_trans_info}" itemDisabled="#{!DialogManager.bean.addTranslationAfter}"/>
      </h:selectOneRadio>      
   </h:panelGrid>
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
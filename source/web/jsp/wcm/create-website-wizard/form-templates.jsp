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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript" src="${pageContext.request.contextPath}/scripts/validation.js">&#160;</script>
<script type="text/javascript">
   var length = 0;
   var outputPathPatterns = new Array();

   window.addEvent('load', accumulateEmptyPatternFields);

   function accumulateEmptyPatternFields()
   {
      for(var i = 0, element = getElement(i); null != element; i++, element = getElement(i))
      {
         if (isEmpty(element))
         {
            addElement(element);
         }
      }
      document.getElementById("dialog:finish-button").disabled = length > 0;
   }

   function getElement(number)
   {
      var id = "dialog:dialog-body:templates:" + number + ":in-01";
      return document.getElementById(id);
   }

   function checkDisabledState(element)
   {
      if (isEmpty(element))
      {
         if ("undefined" == typeof(outputPathPatterns[element.id]))
         {
            addElement(element);
         }
      }
      else
      {
         if ("undefined" != typeof(outputPathPatterns[element.id]))
         {
            length--;
            delete outputPathPatterns[element.id];
         }
      }
      document.getElementById("dialog:finish-button").disabled = length > 0;
   }

   function isEmpty(element)
   {
       var disabledElement = new Object();
       disabledElement.disabled = false;
       validateOutputPathPattern(disabledElement, element, null);
       return disabledElement.disabled;
   }

   function addElement(element)
   {
      length++;
      outputPathPatterns[element.id] = element;
   }
</script>
</f:verbatim>

<h:panelGrid id="grid-1" columns="1" cellpadding="2" cellspacing="2" width="100%">
   <%-- Template selection list --%>
   <h:outputText id="msg-select" styleClass="mainSubText" value="#{msg.website_select_templates}:" />
   <h:panelGroup id="grp-0">
      <a:selectList id="template-list" activeSelect="true" styleClass="selectListTable" itemStyleClass="selectListItem">
         <a:listItems id="items1" value="#{DialogManager.bean.templatesList}" />
         <h:commandButton id="cmd-add" value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{DialogManager.bean.addTemplate}" />
      </a:selectList>
   </h:panelGroup>
   
   <f:verbatim><div style='padding:4px'></div></f:verbatim>
   <h:outputText id="msg-selected" styleClass="mainSubText" value="#{msg.website_selected_templates}:" />
   
   <h:panelGroup id="grp-1" rendered="#{DialogManager.bean.templatesDataModel.rowCount != 0}">
      <h:panelGrid id="grid-2" columns="2" cellspacing="2" width="100%">
         <h:dataTable id="templates" value="#{DialogManager.bean.templatesDataModel}" var="row" 
                      rowClasses="selectedItemsRow,selectedItemsRowAlt"
                      styleClass="selectedItems" headerClass="selectedItemsHeader"
                      cellspacing="0" cellpadding="4" width="100%">
            <h:column id="col1">
               <f:facet name="header">
                  <h:outputText id="head-1" value="#{msg.name}" />
               </f:facet>
               <f:verbatim>
                  <img style="float:left" src="<%=request.getContextPath()%>/images/icons/template_large.gif" />
               </f:verbatim>
            </h:column>
            <h:column id="col2">
               <h:outputText id="msg-01" value="#{row.title}" />
            </h:column>
            <h:column>
               <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText id="head-2" value="#{msg.output_path_pattern}" />
               </f:facet>
               <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" /><h:outputLabel value=" " />
               <h:inputText id="in-01" value="#{row.outputPathPattern}" size="70" maxlength="1024" 
                                       onchange="javascript:checkDisabledState(this);"
                                       onkeyup="javascript:checkDisabledState(this);" />
            </h:column>
            <h:column>
               <a:actionLink id="act-01" actionListener="#{DialogManager.bean.removeTemplate}" image="/images/icons/delete.gif"
                             value="#{msg.remove}" showLink="false" style="padding:4px" />
            </h:column>
         </h:dataTable>
         <h:graphicImage id="img-help" value="/images/icons/Help_icon.gif" style="vertical-align:-20%;padding-left:8px;cursor:help" onclick="javascript:toggleOutputPathPatternHelp()" />
      </h:panelGrid>
      <f:verbatim>
         <c:import url="/jsp/wcm/output-path-pattern-help.jsp" />
      </f:verbatim>
   </h:panelGroup>
   
   <a:panel id="no-items" rendered="#{DialogManager.bean.templatesDataModel.rowCount == 0}">
      <h:panelGrid id="grid-none" width="100%" columns="1" cellpadding="2"
            styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
   
</h:panelGrid>

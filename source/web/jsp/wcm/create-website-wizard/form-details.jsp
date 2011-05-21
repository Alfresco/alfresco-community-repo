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

<script type="text/javascript">
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:title").focus();
      checkDisabledState();
   }

   function checkDisabledState()
   {
      var disabledElement = document.getElementById('dialog:finish-button');
      var outputPathInput = document.getElementById('dialog:dialog-body:filepattern');
      var additionalConditionInput = document.getElementById('dialog:dialog-body:title');
      var description = document.getElementById("dialog:dialog-body:description");
      validateOutputPathPattern(disabledElement, outputPathInput, additionalConditionInput);
      disabledElement.disabled = (disabledElement.disabled || description.value.length > 1024 || !validateName(additionalConditionInput, "", false));
   }
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" cellspacing="2" width="100%">
   <%-- Form properties --%>
   <h:panelGroup>
      <f:verbatim>
      <table cellpadding="3" cellspacing="2" border="0" width="100%">
         <tr>
            <td class="wizardSectionHeading">
               </f:verbatim>
               <h:outputText value="#{msg.properties}"/>
               <f:verbatim>
            </td>
         </tr>
      </table>
      <table cellpadding="3" cellspacing="2" border="0">
         <tr>
            <td align="left" width=16>
               </f:verbatim>
               <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
               <f:verbatim>
            </td>
            <td>
               </f:verbatim>
               <h:outputText value="#{msg.title}:" />
               <f:verbatim>
            </td>
            <td>
               </f:verbatim>
               <h:inputText id="title" value="#{DialogManager.bean.title}" size="45" maxlength="1024" onchange="javascript:checkDisabledState();" onkeyup="javascript:checkDisabledState();" />
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td></td>
            <td valign="top">
               </f:verbatim>
               <h:outputText value="#{msg.description}:"/>
               <f:verbatim>
            </td>
            <td>
               </f:verbatim>
               <h:inputTextarea id="description" value="#{DialogManager.bean.description}" rows="3" cols="42" onchange="javascript:checkDisabledState();" onkeyup="javascript:checkDisabledState();" />
               <f:verbatim>
            </td>
         </tr>
      </table>
      </f:verbatim>
   </h:panelGroup>
   
   <%-- Save location --%>
   <h:panelGroup>
      <f:verbatim>
      <table cellpadding="3" cellspacing="2" border="0" width="100%">
         <tr>
            <td class="wizardSectionHeading">
               </f:verbatim>
               <h:outputText value="#{msg.website_save_location}"/>
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td>
               </f:verbatim>
               <h:outputText value="#{msg.website_save_location_info}:" />
               <f:verbatim>
            </td>
         </tr>
      </table>
      <table cellpadding="3" cellspacing="2" border="0">
         <tr>
            <td align="left" width="16">
               </f:verbatim>
               <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
               <f:verbatim>
            </td>
            <td>
               <nobr>
               </f:verbatim>
               <h:outputText value="#{msg.output_path_pattern}:"/>
               <f:verbatim>
               </nobr>
               </f:verbatim>
               <h:inputText id="filepattern" value="#{DialogManager.bean.outputPathPattern}" size="70" maxlength="1024" onchange="javascript:checkDisabledState();" onkeyup="javascript:checkDisabledState();" />
               <h:graphicImage value="/images/icons/Help_icon.gif" style="vertical-align:-20%;padding-left:8px;cursor:help" onclick="javascript:toggleOutputPathPatternHelp()" />
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td></td>
            <td>
               <c:import url="/jsp/wcm/output-path-pattern-help.jsp" />
            </td>
         </tr>
      </table>
      </f:verbatim>
   </h:panelGroup>
   
   <%-- Workflow --%>
   <h:panelGroup>
      <f:verbatim>
      <table cellpadding="3" cellspacing="2" border="0" width="100%">
         <tr>
            <td class="wizardSectionHeading">
               </f:verbatim>
               <h:outputText value="#{msg.website_workflow}"/>
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td>
               </f:verbatim>
               <h:outputText value="#{msg.website_workflow_info}:" />
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td>
               </f:verbatim>
               <%-- Workflow selection list - scrollable DIV area --%>
               <h:panelGroup>
                  <f:verbatim><div style="height:144px;*height:148px;width:100%;overflow:auto" class='selectListTable'></f:verbatim>
                  <a:selectList id="workflow-list" multiSelect="false" style="width:100%" itemStyleClass="selectListItem"
                        value="#{DialogManager.bean.workflowSelectedValue}">
                     <a:listItems value="#{DialogManager.bean.workflowList}" />
                  </a:selectList>
                  <f:verbatim></div></f:verbatim>
               </h:panelGroup>
               <f:verbatim>
            </td>
         </tr>
      </table>
      </f:verbatim>
   </h:panelGroup>
   
</h:panelGrid>

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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script type="text/javascript">
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:title").focus();
      checkButtonState();
   }

   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:title").value.length == 0)
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

<h:panelGrid columns="1" cellpadding="2" cellpadding="2" width="100%">
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
               <h:inputText id="title" value="#{DialogManager.bean.title}" size="35" maxlength="1024" onkeyup="javascript:checkButtonState();" />
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td></td>
            <td>
               </f:verbatim>
               <h:outputText value="#{msg.description}:"/>
               <f:verbatim>
            </td>
            <td>
               </f:verbatim>
               <h:inputText id="description" value="#{DialogManager.bean.description}" size="35" maxlength="1024" />
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
            <td>
               <nobr>
               </f:verbatim>
               <h:outputText value="#{msg.output_path_pattern}:"/>
               <f:verbatim>
               </nobr>
            </td>
            <td>
               </f:verbatim>
               <h:inputText id="filepattern" value="#{DialogManager.bean.outputPathPattern}" size="65" maxlength="1024" />
               <h:graphicImage value="/images/icons/Help_icon.gif" style="vertical-align:-20%;padding-left:8px;cursor:help" onclick="javascript:toggleOutputPathPatternHelp()" />
               <f:verbatim>
            </td>
         </tr>
         <tr>
            <td></td>
            <td>
               <jsp:directive.include file="/jsp/wcm/output-path-pattern-help.jsp"/>
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
                  <f:verbatim><div style="height:108px;*height:112px;width:300px;overflow:auto" class='selectListTable'></f:verbatim>
                  <a:selectList id="workflow-list" multiSelect="false" style="width:276px" itemStyleClass="selectListItem"
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

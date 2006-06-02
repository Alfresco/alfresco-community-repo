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

<f:verbatim>
<script type="text/javascript">
   function itemSelected(inputField)
   {
      if (inputField.selectedIndex == 0)
      {
         document.getElementById("wizard:wizard-body:set-add-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:wizard-body:set-add-button").disabled = false;
      }
         
      // also check to see if a no parameters option has been selected, if it has, change
      // the explanation text and the button label
      var short_text = "</f:verbatim><a:outputText value='#{msg.click_add_to_list}' /><f:verbatim>";
      var long_text = "</f:verbatim><a:outputText value='#{msg.click_set_and_add}' /><f:verbatim>";
      var short_label = "</f:verbatim><a:outputText value='#{msg.add_to_list_button}' encodeForJavaScript='true' /><f:verbatim>";
      var long_label = "</f:verbatim><a:outputText value='#{msg.set_and_add_button}' encodeForJavaScript='true' /><f:verbatim>";
      
      if (inputField.value == "no-condition")
      {
         document.getElementById("wizard:wizard-body:set-add-button").value = decodeURI(short_label);
         document.getElementById("wizard:wizard-body:instruction-text").innerHTML = short_text;
      }
      else
      {
         document.getElementById("wizard:wizard-body:set-add-button").value = decodeURI(long_label);
         document.getElementById("wizard:wizard-body:instruction-text").innerHTML = long_text;
      }
   }
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td>1.</td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.select_action}"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td width="98%">
         </f:verbatim>
         <h:selectOneMenu value="#{WizardManager.bean.action}" 
                          id="action" onchange="javascript:itemSelected(this);">
            <f:selectItems value="#{WizardManager.bean.actions}" />
         </h:selectOneMenu>
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td>2.</td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.click_set_and_add}" id="instruction-text"/>
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td>
         </f:verbatim>
         <h:commandButton id="set-add-button" value="#{msg.set_and_add_button}" 
                           action="#{WizardManager.bean.promptForActionValues}"
                           disabled="true"/>
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td colspan='2'>
         </f:verbatim>
         <h:outputText value="#{msg.selected_actions}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td colspan='2'>
         </f:verbatim>
         <h:dataTable value="#{WizardManager.bean.allActionsDataModel}" var="row"
                      rowClasses="selectedItemsRow,selectedItemsRowAlt"
                      styleClass="selectedItems" headerClass="selectedItemsHeader"
                      cellspacing="0" cellpadding="4" 
                      rendered="#{WizardManager.bean.allActionsDataModel.rowCount != 0}">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="#{msg.summary}" />
               </f:facet>
               <h:outputText value="#{row.actionSummary}" />
               <h:outputText value="&nbsp;&nbsp;" escape="false"/>
            </h:column>
            <h:column>
               <a:actionLink action="#{WizardManager.bean.removeAction}" image="/images/icons/delete.gif"
                             value="#{msg.remove}" showLink="false" style="padding-left:6px;padding-right:2px" />
               <a:actionLink action="#{WizardManager.bean.editAction}" image="/images/icons/edit_icon.gif"
                             value="#{msg.change}" showLink="false" rendered='#{row.noParamsMarker == null}' />
            </h:column>
         </h:dataTable>
         <a:panel id="no-items" rendered="#{WizardManager.bean.allActionsDataModel.rowCount == 0}">
            <f:verbatim>
            <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
               <tr>
                  <td colspan='2' class='selectedItemsHeader'>
                     </f:verbatim>
                     <h:outputText id="no-items-name" value="#{msg.summary}" />
                     <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td class='selectedItemsRow'>
                     </f:verbatim>
                     <h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
                     <f:verbatim>
                  </td>
               </tr>
            </table>
            </f:verbatim>
         </a:panel>
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>
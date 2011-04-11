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
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_condition_in_category">



<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>


<script type="text/javascript">
   function itemSelected(inputField)
   {
      if (inputField.selectedIndex == 0)
      {
         document.getElementById("composite-condition:set-add-button").disabled = true;
      }
      else
      {
         document.getElementById("composite-condition:set-add-button").disabled = false;
      }
      
      // also check to see if the 'no-condition' option has been selected, if it has, change
      // the explanation text and the button label
      var short_text = "<h:outputText value='#{msg.click_add_to_list}' />";
      var long_text = "<h:outputText value='#{msg.click_set_and_add}' />";
      var short_label = "<h:outputText value='#{msg.add_to_list_button}' />";
      var long_label = "<h:outputText value='#{msg.set_and_add_button}' />";
      
      if (inputField.value == "no-condition")
      {
         document.getElementById("composite-condition:set-add-button").value = short_label;
         document.getElementById("composite-condition:instruction-text").innerHTML = short_text;
      }
      else
      {
         document.getElementById("composite-condition:set-add-button").value = long_label;
         document.getElementById("composite-condition:instruction-text").innerHTML = long_text;
      }
   }
</script>

   
   <h:form acceptcharset="UTF-8" id="composite-condition">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2" width="100%">
      
      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="<h:outputText value="#{NavigationBean.workAreaWidth}" />">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/new_rule_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"> <h:outputText value="#{WizardManager.title}" /> - <h:outputText value="#{msg.composite_condition_page_title}" /> </div>
                              <div class="mainSubText"><h:outputText value="#{msg.composite_condition_page_description}" /></div>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
               </tr>
               
               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                           
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                           
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td>1.</td>
      <td>
         <h:outputText value="#{msg.select_condition}"/>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td width="98%">
         <h:selectOneMenu value="#{WizardManager.bean.condition}" 
                          id="condition" onchange="javascript:itemSelected(this);">
            <f:selectItems value="#{WizardManager.bean.compositeConditions}" />
         </h:selectOneMenu>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td>2.</td>
      <td>
         <h:outputText value="#{msg.click_set_and_add}" id="instruction-text"/>
      </td>
   </tr>
   <tr>
      <td>&nbsp;</td>
      <td>
         <h:commandButton id="set-add-button" value="#{msg.set_and_add_button}" 
                          action="#{WizardManager.bean.promptForConditionValues}"
                          disabled="true"/>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td colspan='2'>
         <h:outputText value="#{msg.composite_condition_page_selected}" />
                                       <h:selectBooleanCheckbox value="#{WizardManager.bean.conditionProperties.orconditions}"/>
                                       <h:outputText value="#{msg.composite_condition_page_or}"/>
      </td>
   </tr>
   <tr>
      <td colspan='2'>
         <h:dataTable value="#{WizardManager.bean.allCompositeConditionsDataModel}" var="row"
                      rowClasses="selectedItemsRow,selectedItemsRowAlt"
                      styleClass="selectedItems" headerClass="selectedItemsHeader"
                      cellspacing="0" cellpadding="4" 
                      rendered="#{WizardManager.bean.allCompositeConditionsDataModel.rowCount != 0}">
            <h:column>
               <f:facet name="header">
                  <h:outputText value="#{msg.summary}" />
               </f:facet>
               <h:outputText value="#{row.conditionSummary}" />
            </h:column>
            <h:column>
               <a:actionLink action="#{WizardManager.bean.removeCondition}" image="/images/icons/delete.gif"
                             value="#{msg.remove}" showLink="false" style="padding-left:6px;padding-right:2px" />
               <a:actionLink action="#{WizardManager.bean.editCondition}" image="/images/icons/edit_icon.gif"
                             value="#{msg.change}" showLink="false" rendered='#{row.noParamsMarker == null}' />
            </h:column>
         </h:dataTable>
         <a:panel id="no-items" rendered="#{WizardManager.bean.allCompositeConditionsDataModel.rowCount == 0}">
            <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
               <tr>
                  <td colspan='2' class='selectedItemsHeader'>
                     <h:outputText id="no-items-name" value="#{msg.summary}" />
                  </td>
               </tr>
               <tr>
                  <td class='selectedItemsRow'>
                     <h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
                  </td>
               </tr>
            </table>
         </a:panel>
      </td>
   </tr>
   <tr>
      <td colspan='2'>

   			<table><tr>
                                       <td><h:selectBooleanCheckbox value="#{WizardManager.bean.conditionProperties.notcondition}"/></td>
                                       <td><h:outputText value="#{msg.not_condition_result}"/></td>
                                       </tr></table>
      </td>
   </tr>
 

</table>

                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.ok}" action="#{WizardManager.bean.finishAddingCompositeCondition}" styleClass="wizardButton" 
                                                        disabled="#{WizardManager.bean.conditionProperties == null}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="cancel-button" value="#{msg.cancel_button}" action="#{WizardManager.bean.cancelAddCondition}" 
                                                        styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>

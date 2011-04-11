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

<r:page titleId="title_condition_contains_text">

<script language="JavaScript1.2">
function checkButtonState()
   {
	  var inputField1 = document.getElementById('contains-text-condition:pattern'); 
	  var inputField2 = document.getElementById('contains-text-condition:qname'); 
	  var disabled = (inputField1.value.length == 0 || inputField2.value.length == 0);
      document.getElementById("contains-text-condition:ok-button").disabled = disabled;
   }
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <r:loadBundle var="msg"/>
   
   <h:form acceptcharset="UTF-8" id="contains-text-condition">
   
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
                              <div class="mainTitle"><h:outputText value="#{WizardManager.title}" /></div>
                              <div class="mainSubText"><h:outputText value="#{WizardManager.description}" /></div>
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
                                    <td colspan="2" class="mainSubTitle"><h:outputText value="#{msg.set_condition_values}" /></td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2" class="mainSubText">
					<h:outputText value="#{msg.enter_integer_condition}"/>
				    </td>
                                 </tr>
<!--
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td width="200px"> 
                                        <h:outputText value="#{msg.select_default_qname}"/>:&nbsp;
					</td> <td>
                                        <select id="select_default_qname">
                                           <option value="select">select qname</option>
                                           <option value="cm:name">cm:name</option>
                                           <option value="cm:description">cm:description</option>
                                        </select>
                                    </td>
                                 </tr>
-->
                                 <tr>
                                    <td width="200px"> 
                                        <h:outputText value="#{msg.integer_property_condition_property}"/>:&nbsp;
				    </td> <td>
 					<h:inputText id="qname" value="#{WizardManager.bean.conditionProperties.qname}"
 					             onkeyup="javascript:checkButtonState();" size="35" maxlength="1024" />
                                    </td>
                                 </tr>
 				<tr>
                                    <td width="200px"> 
                                        <h:outputText value="#{msg.property_condition_operation}"/>:&nbsp;
				     </td> <td>
   					<h:selectOneMenu value="#{WizardManager.bean.conditionProperties.operation}">
  						<f:selectItem itemValue="EQUALS" itemLabel="#{msg.property_condition_equals}"/>
  						<f:selectItem itemValue="GREATER_THAN" itemLabel="#{msg.property_condition_greaterthan}"/>
  						<f:selectItem itemValue="GREATER_THAN_EQUAL" itemLabel="#{msg.property_condition_greaterthanequals}"/>
  						<f:selectItem itemValue="LESS_THAN" itemLabel="#{msg.property_condition_lessthan}"/>
  						<f:selectItem itemValue="LESS_THAN_EQUAL" itemLabel="#{msg.property_condition_lessthanequals}"/>
                                       </h:selectOneMenu>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.property_condition_value}"/>:&nbsp;
				     </td> <td>
                                       <h:inputText id="pattern" value="#{WizardManager.bean.conditionProperties.containstext}" 
                                                    onkeyup="javascript:checkButtonState();" size="35" maxlength="1024" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="2">
                                       <table><tr>
                                       <td><h:selectBooleanCheckbox value="#{WizardManager.bean.conditionProperties.notcondition}"/></td>
                                       <td><h:outputText value="#{msg.not_condition_result}"/></td>
                                       </tr></table>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.ok}" action="#{WizardManager.bean.addCondition}" 
                                                        styleClass="wizardButton" 
                                                        disabled="#{WizardManager.bean.conditionProperties.containstext == null || WizardManager.bean.conditionProperties.qname == null}" />
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

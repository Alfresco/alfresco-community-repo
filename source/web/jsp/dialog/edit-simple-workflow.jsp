<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<script language="JavaScript1.2">
   
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:approve-step-name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:approve-step-name").value.length == 0 ||
          document.getElementById("dialog:dialog-body:client-approve-folder_selected").value.length == 0 ||
          rejectValid() == false)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
   
   function rejectValid()
   {
      var result = true;
      
      if (document.forms['dialog']['dialog:dialog-body:reject-step-present'][0].checked && 
          (document.getElementById("dialog:dialog-body:reject-step-name").value.length == 0 ||
           document.getElementById("dialog:dialog-body:client-reject-folder_selected").value.length == 0))
      {
         result = false;
      }
      
      return result;
   }
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
   	<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText
   		value="#{msg.approve_flow}" /><f:verbatim></td>
   </tr>
   <tr>
   	<td><nobr></f:verbatim><h:outputText value="#{msg.name_approve_step}" /><f:verbatim>:</nobr></td>
   	<td width="90%"></f:verbatim><h:inputText id="approve-step-name"
   		value="#{EditSimpleWorkflowDialog.workflowProperties.approveStepName}"
   		onkeyup="javascript:checkButtonState();" /><f:verbatim></td>
   </tr>
   <tr>
   	<td colspan="2" class="paddingRow"></td>
   </tr>
   <tr>
   	<td colspan="2"></f:verbatim><h:outputText
   		value="#{msg.choose_copy_move_location}" /><f:verbatim></td>
   <tr>
   	<td colspan="2">
   	<table cellpadding="2" cellspacing="2" border="0">
   		<tr>
   			<td valign="top"></f:verbatim><h:selectOneRadio
   				value="#{EditSimpleWorkflowDialog.workflowProperties.approveAction}">
   				<f:selectItem itemValue="move" itemLabel="Move" />
   				<f:selectItem itemValue="copy" itemLabel="Copy" />
   			</h:selectOneRadio><f:verbatim></td>
   			<td style="padding-left:6px;"></td>
   			<td valign="top" style="padding-top:10px;"></f:verbatim><h:outputText
   				value="#{msg.to}" /><f:verbatim>:</td>
   			<td style="padding-left:6px;"></td>
   			<td style="padding-top:6px;"></f:verbatim><r:spaceSelector
   				id="client-approve-folder"
   				label="#{msg.select_destination_prompt}"
   				value="#{EditSimpleWorkflowDialog.workflowProperties.approveFolder}"
   				initialSelection="#{NavigationBean.currentNodeId}"
   				styleClass="selector" /><f:verbatim></td>
   		</tr>
   	</table>
   	</td>
   </tr>
   <tr>
   	<td colspan="2" class="paddingRow"></td>
   </tr>
   <tr>
   	<td colspan="2" class="wizardSectionHeading"></f:verbatim><h:outputText
   		value="#{msg.reject_flow}" /><f:verbatim></td>
   </tr>
   <tr>
   	<td colspan="2"></f:verbatim><h:outputText value="#{msg.select_reject_step}" /><f:verbatim></td>
   </tr>
   <tr>
   	<td></f:verbatim><h:selectOneRadio id="reject-step-present"
   		value="#{EditSimpleWorkflowDialog.workflowProperties.rejectStepPresent}"
   		onclick="javascript:checkButtonState();">
   		<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
   		<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
   	</h:selectOneRadio><f:verbatim></td>
   </tr>
   <tr>
   	<td colspan="2">
   	<table cellpadding="0" cellspacing="0" border="0">
   		<tr>
   			<td style="padding-left:24px;"></td>
   			<td>
   			<table cellpadding="2" cellspacing="2" border="0">
   				<tr>
   					<td><nobr> </f:verbatim><h:outputText
   						value="#{msg.name_reject_step}" /><f:verbatim>:&nbsp; </f:verbatim><h:inputText
   						id="reject-step-name"
   						value="#{EditSimpleWorkflowDialog.workflowProperties.rejectStepName}"
   						onkeyup="javascript:checkButtonState();" /><f:verbatim> </nobr></td>
   				</tr>
   				<tr>
   					<td class="paddingRow"></td>
   				</tr>
   				<tr>
   					<td></f:verbatim><h:outputText value="#{msg.choose_copy_move_location}" /><f:verbatim></td>
   				<tr>
   					<td>
   					<table cellpadding="2" cellspacing="2" border="0">
   						<tr>
   							<td valign="top"></f:verbatim><h:selectOneRadio
   								value="#{DocumentDetailsBean.workflowProperties.rejectAction}">
   								<f:selectItem itemValue="move" itemLabel="#{msg.move}" />
   								<f:selectItem itemValue="copy" itemLabel="#{msg.copy}" />
   							</h:selectOneRadio><f:verbatim></td>
   							<td style="padding-left:6px;"></td>
   							<td valign="top" style="padding-top:10px;"></f:verbatim><h:outputText
   								value="#{msg.to}" /><f:verbatim>:</td>
   							<td style="padding-left:6px;"></td>
   							<td style="padding-top:6px;"></f:verbatim><r:spaceSelector
   								id="client-reject-folder"
   								label="#{msg.select_destination_prompt}"
   								value="#{EditSimpleWorkflowDialog.workflowProperties.rejectFolder}"
   								initialSelection="#{NavigationBean.currentNodeId}"
   								styleClass="selector" /><f:verbatim></td>
   						</tr>
   					</table>
   					</td>
   				</tr>
   			</table>
   			</td>
   		</tr>
   	</table>
   	</td>
   </tr>
</table>
</f:verbatim>
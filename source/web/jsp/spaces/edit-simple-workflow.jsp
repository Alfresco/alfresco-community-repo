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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<f:verbatim>
<script type="text/javascript">
   
   addEventToElement(window, 'load', pageLoaded, false);
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:approve-step-name").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:approve-step-name").value.length == 0 ||
          document.getElementById("clientApproveFolder-value").value.length == 0 ||
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
           document.getElementById("clientRejectFolder-value").value.length == 0))
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
				value="#{DialogManager.bean.workflowProperties.approveStepName}"
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
						value="#{DialogManager.bean.workflowProperties.approveAction}">
						<f:selectItem itemValue="move" itemLabel="Move" />
						<f:selectItem itemValue="copy" itemLabel="Copy" />
					</h:selectOneRadio><f:verbatim></td>
					<td style="padding-left:6px;"></td>
					<td valign="top" style="padding-top:10px;"></f:verbatim><h:outputText
						value="#{msg.to}" /><f:verbatim>:</td>
					<td style="padding-left:6px;"></td>
					<td style="padding-top:6px;"></f:verbatim><r:ajaxFolderSelector
   			      id="clientApproveFolder"
   			      label="#{msg.select_destination_prompt}" 
                  value="#{DialogManager.bean.workflowProperties.approveFolder}" 
                  initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
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
				value="#{DialogManager.bean.workflowProperties.rejectStepPresent}"
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
								value="#{DialogManager.bean.workflowProperties.rejectStepName}"
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
										value="#{DialogManager.bean.workflowProperties.rejectAction}">
										<f:selectItem itemValue="move" itemLabel="#{msg.move}" />
										<f:selectItem itemValue="copy" itemLabel="#{msg.copy}" />
									</h:selectOneRadio><f:verbatim></td>
									<td style="padding-left:6px;"></td>
									<td valign="top" style="padding-top:10px;"></f:verbatim><h:outputText
										value="#{msg.to}" /><f:verbatim>:</td>
									<td style="padding-left:6px;"></td>
									<td style="padding-top:6px;"></f:verbatim><r:ajaxFolderSelector
   							      id="clientRejectFolder"
               			      label="#{msg.select_destination_prompt}" 
                              value="#{DialogManager.bean.workflowProperties.rejectFolder}" 
                              initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
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
<#--
 #%L
 Alfresco Records Management Module
 %%
 Copyright (C) 2005 - 2021 Alfresco Software Limited
 %%
 This file is part of the Alfresco software.
 -
 If the software was purchased under a paid Alfresco license, the terms of
 the paid license agreement will prevail.  Otherwise, the software is
 provided under the following open source license terms:
 -
 Alfresco is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 -
 Alfresco is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 -
 You should have received a copy of the GNU Lesser General Public License
 along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 #L%
-->
<#import "dispositionactiondefinition.lib.ftl" as actionDefLib/>

<@scheduleJSON schedule=schedule/>

<#macro scheduleJSON schedule>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"url": "${schedule.url}",
		"nodeRef": "${schedule.nodeRef}",
		<#if schedule.authority??>"authority": "${schedule.authority}",</#if>
		<#if schedule.instructions??>"instructions": "${schedule.instructions}",</#if>
		"unpublishedUpdates" : ${schedule.unpublishedUpdates?string},
		"publishInProgress" : ${schedule.publishInProgress?string},
		"recordLevelDisposition": ${schedule.recordLevelDisposition?string},
		"canStepsBeRemoved": ${schedule.canStepsBeRemoved?string},
		"actionsUrl": "${schedule.actionsUrl}",
		"actions": 
		[
			<#list schedule.actions as action>
			<@actionDefLib.actionJSON action=action/>
			<#if action_has_next>,</#if>
			</#list>
		]
	}
}
</#escape>
</#macro>

<#--
 #%L
 Alfresco Records Management Module
 %%
 Copyright (C) 2005 - 2022 Alfresco Software Limited
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
<#macro actionJSON action>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id": "${action.id}",
		"url": "${action.url}",
		"index": ${action.index},
		"name": "${action.name}",
		"label": "${action.label}",
		<#if (action.name == "destroy") && action.ghostOnDestroy??>"ghostOnDestroy": "${action.ghostOnDestroy}",</#if>
		<#if action.description??>"description": "${action.description}",</#if>
		<#if action.period??>"period": "${action.period}",</#if>
		<#if action.periodProperty??>"periodProperty": "${action.periodProperty}",</#if>
		<#if action.location??>"location": "${action.location}",</#if>
        <#if action.combineDispositionStepConditions??>"combineDispositionStepConditions": "${action.combineDispositionStepConditions?string}",</#if>
		<#if action.events??>"events": [<#list action.events as event>"${event}"<#if event_has_next>,</#if></#list>],</#if>
		"eligibleOnFirstCompleteEvent": ${action.eligibleOnFirstCompleteEvent?string}
	}
</#escape>
</#macro>

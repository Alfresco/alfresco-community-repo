<#--
 #%L
 This file is part of Alfresco.
 %%
 Copyright (C) 2005 - 2016 Alfresco Software Limited
 %%
 Alfresco is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Alfresco is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
  
 You should have received a copy of the GNU Lesser General Public License
 along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 #L%
-->
<#macro listsJSON lists>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"dispositionActions":
		{
			"url": "${lists.dispositionActions.url}",
			"items":
			[
				<#list lists.dispositionActions.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"events":
		{
			"url": "${lists.events.url}",
			"items":
			[
				<#list lists.events.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}",
					"automatic": ${item.automatic?string}
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"periodTypes":
		{
			"url": "${lists.periodTypes.url}",
			"items":
			[
				<#list lists.periodTypes.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"periodProperties":
		{
			"url": "${lists.periodProperties.url}",
			"items":
			[
				<#list lists.periodProperties.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		},
		"auditEvents":
		{
			"url": "${lists.auditEvents.url}",
			"items":
			[
				<#list lists.auditEvents.items as item>
				{
					"label": "${item.label}",
					"value": "${item.value}"
				}<#if item_has_next>,</#if>
				</#list>
			]
		}
	}
}
</#escape>
</#macro>

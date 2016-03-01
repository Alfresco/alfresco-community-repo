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
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
	{
		"nodeName": "${nodeName!""}",
		"nodeTitle": "${nodeTitle!""}",
		"customReferencesFrom":
		[
			<#list customRefsFrom as ref>
			{
				<#assign keys = ref?keys>
				<#list keys as key>"${key}": "${ref[key]}"<#if key_has_next>,</#if></#list>
			}<#if ref_has_next>,</#if>
			</#list>
		],
		"customReferencesTo":
		[
			<#list customRefsTo as ref>
			{
				<#assign keys = ref?keys>
				<#list keys as key>"${key}": "${ref[key]}"<#if key_has_next>,</#if></#list>
			}<#if ref_has_next>,</#if>
			</#list>
		]
	}
}
</#escape>

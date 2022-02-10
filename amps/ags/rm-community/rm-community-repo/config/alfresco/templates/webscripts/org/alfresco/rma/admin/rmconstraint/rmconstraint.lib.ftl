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
<#-- renders an rm constraint object -->

<#macro constraintSummaryJSON constraint>
<#escape x as jsonUtils.encodeJSONString(x)>
		{
			"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name}",
			"constraintName" : "${constraint.name}",
			"constraintTitle" : "${msg(constraint.title)}"
		}
</#escape>
</#macro>

<#macro constraintJSON constraint>
<#escape x as jsonUtils.encodeJSONString(x)>
		{
			"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name}",
			"constraintName" : "${constraint.name}",
			"caseSensitive" :  "${constraint.caseSensitive?string("true", "false")}",
			"constraintTitle" : "${constraint.title}",
			"allowedValues" : [ <#list constraint.allowedValues as allowedValue> "${allowedValue}" <#if allowedValue_has_next>,</#if> </#list> ]
		}
</#escape>
</#macro>

<#macro constraintWithValuesJSON constraint>
<#escape x as jsonUtils.encodeJSONString(x)>
		{
			"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name}",
			"constraintName" : "${constraint.name}",
			"caseSensitive" :  "${constraint.caseSensitive?string("true", "false")}",
			"constraintTitle" : "${msg(constraint.title)}",
			"values" : [
				<#list constraint.values as value>
				{
					"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name + "/values/" + value.valueName}",
					"valueName":"${value.valueName}",
					"valueTitle":"${value.valueTitle}",
					"authorities" : [ <#list value.authorities as authority> { "authorityName" : "${authority.authorityName}", "authorityTitle" : "${authority.authorityTitle}"} <#if authority_has_next>,</#if></#list>]
				}<#if value_has_next>,</#if>
				</#list>
			]
		}
</#escape>
</#macro>

<#macro constraintWithValueJSON constraint value>
<#escape x as jsonUtils.encodeJSONString(x)>
		{
			"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name + "/values/" + value.valueName}",
			"constraintName" : "${constraint.name}",
			"constraintTitle" : "${constraint.title}",
			"value" :
			{
				"url" : "${url.serviceContext + "/api/rma/admin/rmconstraints/" + constraint.name + "/values/" + value.valueName}",
				"valueName":"${value.valueName}",
				"valueTitle":"${value.valueTitle}",
				"authorities" : [ <#list value.authorities as authority> { "authorityName" : "${authority.authorityName}", "authorityTitle" : "${authority.authorityTitle}"} <#if authority_has_next>,</#if></#list>]
			}
		}
</#escape>
</#macro>

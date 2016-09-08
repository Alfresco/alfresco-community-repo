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
<#macro usageJSON>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "lastUpdate" : <#if lastUpdate??>${lastUpdate?c}<#else>null</#if>,
   "users" : <#if users??>${users?c}<#else>null</#if>,
   "documents" : <#if documents??>${documents?c}<#else>null</#if>,
   "licenseMode" : "${licenseMode}",
   "readOnly" : ${readOnly?string("true","false")},
   "updated" : ${updated?string("true","false")},
   "licenseValidUntil" : <#if licenseValidUntil??>${licenseValidUntil?c}<#else>null</#if>,
   "licenseHolder" : "<#if licenseHolder??>${licenseHolder}</#if>",
   "level" : ${level?c},
   "warnings": [<#list warnings as x>"${x}"<#if x_has_next>, </#if></#list>],
   "errors": [<#list errors as x>"${x}"<#if x_has_next>, </#if></#list>]
}
</#escape>
</#macro>
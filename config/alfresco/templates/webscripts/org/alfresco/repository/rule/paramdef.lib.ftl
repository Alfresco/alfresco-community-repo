<#macro paramDefJSON paramDef>
{
   "name" : "${paramDef.name}",
   <#if paramDef.displayLabel?exists>
      "displayLabel" : "${paramDef.displayLabel}",
   </#if>
   "type" : "${paramDef.type}",
   "multiValued" : ${paramDef.multiValued?string("true", "false")},
   "mandatory" : "${paramDef.mandatory?string("true", "false")}",
   "url" : "${url.serviceContext + "/api/rules/parameterdefs/" + paramDef.name}"
  }
</#macro>
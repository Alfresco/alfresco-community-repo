<#macro paramDefJSON paramDef>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name" : "${paramDef.name}",
         "displayLabel" : "${paramDef.displayLabel}",
         "type" : "${paramDef.type}",
         "multiValued" : ${paramDef.multiValued?string("true", "false")},
         "mandatory" : "${paramDef.mandatory}",
         "url" : "${url.serviceContext + "/api/rules/parameterdefs/" + paramDef.name}"
      }
   </#escape>
</#macro>
<#import "paramdef.lib.ftl" as paramDefLib/>

<#macro conditionDefJSON conditiondef>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name" : "${conditiondef.name}",
         "title" : "${conditiondef.title},
         "description" : "${conditiondef.description}",
         "adhocPropertiesAllowed" : "${conditiondef.adhocPropertiesAllowed?string("true", "false")}",
         "parameterDefinitions" :
         [
         <#list conditiondef.parameterDefinitions as paramDef>
            <@paramDefLib.paramDefJSON paramDef=paramDef/>
            <#if paramDef_has_next>,</#if>
         </#list>
         ],
         "url" : "${url.serviceContext + "/api/rules/conditiondefs/" + conditiondef.name}"
      }
   </#escape>
</#macro>

<#import "paramdef.lib.ftl" as paramDefLib/>

<#macro conditionDefJSON conditiondef>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name" : "${conditiondef.name}",
         <#if conditiondef.title?exists>
            "title" : "${conditiondef.title}",
         </#if>
         <#if conditiondef.description?exists>
            "description" : "${conditiondef.description}",
         </#if>
         "adhocPropertiesAllowed" : "${conditiondef.adhocPropertiesAllowed?string("true", "false")}",
         <#if conditiondef.parameterDefinitions?exists>
            "parameterDefinitions" :
            [
            <#list conditiondef.parameterDefinitions as paramDef>
               <@paramDefLib.paramDefJSON paramDef=paramDef/>
               <#if paramDef_has_next>,</#if>
            </#list>
            ],
         </#if>
         "url" : "${url.serviceContext + "/api/rules/conditiondefs/" + conditiondef.name}"
      }
   </#escape>
</#macro>

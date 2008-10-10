<#import "paramdef.lib.ftl" as paramDefLib/>

<#macro actionDefJSON actiondef>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name" : "${actiondef.name}",
         "title" : "${actiondef.title},
         "description" : "${actiondef.description}",
         "adhocPropertiesAllowed" : "${actiondef.adhocPropertiesAllowed?string("true", "false")}",
         "applicableTypes" :
         [
         <#list actiondef.applicableTypes as applicableType>
            "${applicableType.getLocalName()}"
            <#if applicableType_has_next>,</#if>
         </#list>
         ],
         "parameterDefinitions" :
         [
         <#list actiondef.parameterDefinitions as paramDef>
            <@paramDefLib.paramDefJSON paramDef=paramDef/>
            <#if paramDef_has_next>,</#if>
         </#list>
         ],
         "url" : "${url.serviceContext + "/api/rules/actiondefs/" + actiondef.name}"
      }
   </#escape>
</#macro>

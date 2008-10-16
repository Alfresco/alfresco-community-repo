<#import "paramdef.lib.ftl" as paramDefLib/>

<#macro actionDefJSON actiondef>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "name" : "${actiondef.name}",
         <#if actiondef.title?exists>
            "title" : "${actiondef.title}",
         </#if>
         <#if actiondef.description?exists>
            "description" : "${actiondef.description}",
         </#if>
         "adhocPropertiesAllowed" : "${actiondef.adhocPropertiesAllowed?string("true", "false")}",
         "applicableTypes" :
         [
         <#list actiondef.applicableTypes as applicableType>
            "${applicableType.getLocalName()}"
            <#if applicableType_has_next>,</#if>
         </#list>
         ],
         <#if actiondef.parameterDefinitions?exists>
            "parameterDefinitions" :
            [
            <#list actiondef.parameterDefinitions as paramDef>
               <@paramDefLib.paramDefJSON paramDef=paramDef/>
               <#if paramDef_has_next>,</#if>
            </#list>
            ],
         </#if>
         "url" : "${url.serviceContext + "/api/rules/actiondefs/" + actiondef.name}"
      }
   </#escape>
</#macro>

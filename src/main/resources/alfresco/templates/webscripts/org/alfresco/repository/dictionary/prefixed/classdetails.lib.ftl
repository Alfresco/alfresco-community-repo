<#macro classDefJSON classdef key>
<#local classdefprefix=classdef.name.toPrefixString()?replace(":","/")>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      <#if classdef.name??>"name": "${classdef.name.toPrefixString()}",</#if>
      "isAspect": ${classdef.isAspect()?string},
      "isContainer": ${classdef.isContainer()?string},
      "title": "${classdef.getTitle(messages)!""}",
      "description": "${classdef.getDescription(messages)!""}",
      "parent":
      {
         <#if classdef.parentName??>
         "name": "${classdef.parentName.toPrefixString()}",
         "title": "${classdef.parentName.getLocalName()}",
         "url": "/api/defclasses/${classdef.parentName.toPrefixString()?replace(":","/")}"
         </#if>
      },
      "defaultAspects":
      {
         <#if classdef.defaultAspects??>
         <#list classdef.defaultAspects as aspectdef>
         "${aspectdef.name.toPrefixString()}":
         {
            "name": "${aspectdef.name.toPrefixString()}",
            "title": "${aspectdef.getTitle(messages)!""}",
            "url": "/api/defclasses/${classdefprefix}/property/${aspectdef.name.toPrefixString()?replace(":","/")}"
         }<#if aspectdef_has_next>,</#if>
         </#list>
         </#if>
      },
      "properties":
      {
         <#list propertydefs[key] as propertydef>
         "${propertydef.name.toPrefixString()}":
         {
            "name": "${propertydef.name.toPrefixString()}",
            "title": "${propertydef.getTitle(messages)!""}",
            "description": "${propertydef.getDescription(messages)!""}",
            "dataType": <#if propertydef.dataType??>"${propertydef.dataType.name.toPrefixString()}"<#else>"<unknown>"</#if>,
            "defaultValue": <#if propertydef.defaultValue??>"${propertydef.defaultValue}"<#else>null</#if>,
            "multiValued": ${propertydef.multiValued?string},
            "mandatory": ${propertydef.mandatory?string},
            "enforced": ${propertydef.mandatoryEnforced?string},
            "protected": ${propertydef.protected?string},
            "indexed": ${propertydef.indexed?string},
            "url": "/api/defclasses/${classdefprefix}/property/${propertydef.name.toPrefixString()?replace(":","/")}"
         }<#if propertydef_has_next>,</#if>
         </#list>
      },
      "associations":
      {
         <#assign isfirst=true>
         <#list assocdefs[key] as assocdef>
         <#if !isfirst && !assocdef.isChild()>,</#if>
         <#if !assocdef.isChild()>
         <#assign isfirst=false>
         "${assocdef.name.toPrefixString()}":
         {
            "name": "${assocdef.name.toPrefixString()}",
            "title": "${assocdef.getTitle(messages)!""}",
            "url": "/api/defclasses/${classdefprefix}/association/${assocdef.name.toPrefixString()?replace(":","/")}",
            "source":
            {
               <#if assocdef.getSourceClass().name??>"class": "${assocdef.getSourceClass().name.toPrefixString()}",</#if>
               <#if assocdef.getSourceRoleName()??>"role": "${assocdef.getSourceRoleName().toPrefixString()}",</#if>
               "mandatory": ${assocdef.isSourceMandatory()?string},
               "many": ${assocdef.isSourceMany()?string}
            },
            "target":
            {
               <#if assocdef.getTargetClass().name??>"class": "${assocdef.getTargetClass().name.toPrefixString()}",</#if>
               <#if assocdef.getTargetRoleName()??>"role": "${assocdef.getTargetRoleName().toPrefixString()}",</#if>
               "mandatory": ${assocdef.isTargetMandatory()?string},
               "many": ${assocdef.isTargetMany()?string}
            }
         }
         </#if>
         </#list>
      },
      "childassociations":
      {
         <#assign isfirst=true>
         <#list assocdefs[key] as assocdef>
         <#if !isfirst && assocdef.isChild()>,</#if>
         <#if assocdef.isChild()>
         <#assign isfirst=false>
         "${assocdef.name.toPrefixString()}":
         {
            <#if assocdef.name??>"name": "${assocdef.name.toPrefixString()}",</#if>
            "title": "${assocdef.getTitle(messages)!""}",
            "url": "/api/defclasses/${classdefprefix}/association/${assocdef.name.toPrefixString()?replace(":","/")}",
            "source":
            {
               <#if assocdef.getSourceClass().name??>"class": "${assocdef.getSourceClass().name.toPrefixString()}",</#if>
               "mandatory": ${assocdef.isSourceMandatory()?string},
               "many": ${assocdef.isSourceMany()?string}
            },
            "target":
            {
               <#if assocdef.getTargetClass().name??>"class": "${assocdef.getTargetClass().name.toPrefixString()}",</#if>
               "mandatory": ${assocdef.isTargetMandatory()?string},
               "many": ${assocdef.isTargetMany()?string}
            }
         }
         </#if>
         </#list>
      },
      "url": "/api/defclasses/${classdefprefix}"
   }
</#escape>
</#macro>
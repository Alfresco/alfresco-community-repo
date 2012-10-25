<#import "propertydefinition.lib.ftl" as propertyDefLib/>
[
   <#list propertydefs as propertydefinitions>
      <@propertyDefLib.propertyDefJSON propertydefs=propertydefinitions/>
      <#if propertydefinitions_has_next>,</#if>
   </#list>
]
 
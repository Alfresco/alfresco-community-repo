<#import "propertydefinition.lib.ftl" as propertyDefLib/>
[
<#list propertydefs as propertydefs>
	<@propertyDefLib.propertyDefJSON propertydefs=propertydefs/>
	<#if propertydefs_has_next>,</#if>
</#list>
]		
<#import "propertydefinition.lib.ftl" as propertyDefLib/>
[
<#list propertydefs as propertydefinitions>
	<#if individualproperty?exists>
		<#if propertydefinitions.name == individualproperty.name>
		<@propertyDefLib.propertyDefJSON propertydefs=propertydefinitions/>
		<#break>
		</#if>
	<#else>
		<@propertyDefLib.propertyDefJSON propertydefs=propertydefinitions/>
		<#if propertydefinitions_has_next>,</#if>
	</#if>
</#list>
]		
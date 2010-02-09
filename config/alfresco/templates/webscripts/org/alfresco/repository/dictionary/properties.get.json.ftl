<#import "propertydefinition.lib.ftl" as propertyDefLib/>
<#if propertydefs?exists>
   <#if individualproperty?exists == false>
[
   </#if>
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
   <#if individualproperty?exists == false>
]
   </#if>
<#else>
{}
</#if>
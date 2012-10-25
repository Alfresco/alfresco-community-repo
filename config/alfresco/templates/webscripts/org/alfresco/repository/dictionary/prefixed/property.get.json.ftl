<#import "propertydefinition.lib.ftl" as propertyDefLib/>
<#if propertydefs?exists>
	<@propertyDefLib.propertyDefJSON propertydefs=propertydefs/>
<#else>
	{}
</#if>
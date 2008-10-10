<#import "conditiondef.lib.ftl" as conditionDefLib/>

[
	<#list conditiondefs as conditiondef>
		<@conditionDefLib.conditionDefJSON conditiondef=conditiondef/>
		<#if conditiondef_has_next>,</#if>
	</#list>
]
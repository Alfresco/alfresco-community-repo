<#import "actiondef.lib.ftl" as actionDefLib/>

[
	<#list actiondefs as actiondef>
		<@actionDefLib.actionDefJSON actiondef=actiondef/>
		<#if actiondef_has_next>,</#if>
	</#list>
]
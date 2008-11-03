<#import "assocdefinition.lib.ftl" as assocDefLib/>
[
	<#list assocdefs as assocdefs>
		<#if assocdefs.isChild() == true>
		<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
		<#if assocdefs_has_next>,</#if>
		</#if>
	</#list>
]		
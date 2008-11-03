<#import "assocdefinition.lib.ftl" as assocDefLib/>
[
	<#list assocdefs as assocdefs>
		<#if assocdefs.isChild() == false>
		<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
		<#if assocdefs_has_next>,</#if>
		</#if>
	</#list>
]		
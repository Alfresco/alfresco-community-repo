<#import "assocdefinition.lib.ftl" as assocDefLib/>
[
	<#list assocdefs as assocdefs>
		<#if individualproperty?exists>
			<#if assocdefs.name == individualproperty.name>
				<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
			</#if>	
			<#else>
				<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
				<#if assocdefs_has_next>,</#if>
			</#if>
	</#list>
]
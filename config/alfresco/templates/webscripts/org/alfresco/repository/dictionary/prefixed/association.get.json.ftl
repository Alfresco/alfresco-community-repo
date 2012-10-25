<#import "assocdefinition.lib.ftl" as assocDefLib/>
<#if assocdefs?exists>
<@assocDefLib.assocDefJSON assocdefs=assocdefs/>
<#else>
{}
</#if>		
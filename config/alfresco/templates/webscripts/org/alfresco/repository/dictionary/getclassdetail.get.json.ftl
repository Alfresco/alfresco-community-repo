<#import "classdetails.lib.ftl" as classdetailsDefLib/>
[
<#list classdefs as classdefs>
	<@classdetailsDefLib.classDefJSON classdefs=classdefs key = classdefs_index/>
	<#if classdefs_has_next>,</#if>
</#list>
]		

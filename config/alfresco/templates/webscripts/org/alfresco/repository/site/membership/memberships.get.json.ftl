<#import "membership.lib.ftl" as membershipLib />
<#assign userNames = memberships?keys />
[
<#list userNames as userName>		 	   
	<@membershipLib.membershipJSON site=site role=memberships[userName] person=peoplelist[userName] />
	<#if userName_has_next>,</#if>
</#list>
]
<#-- List memberships Implementation-->
<#import "membership.lib.ftl" as membershipLib />

<#assign userNames = roles?keys />
[
<#list userNames as userName>		 	   
	<@membershipLib.membershipJSON site=site role=roles[userName] authority=authorities[userName] />
	<#if userName_has_next>,</#if>
</#list>
]
<#import "membership.lib.ftl" as membershipLib/>
{
  data:[
  <#if memberships??>
	<#assign userNames = memberships?keys />
    <#list userNames as userName>		 	   
	   <@membershipLib.membershipJSON webproject=webproject role=memberships[userName] person=people[userName]/>
	   <#if userName_has_next>,</#if>
    </#list>
  </#if>
  ]
}

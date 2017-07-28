<#import "membership.lib.ftl" as membershipLib/>
<@membershipLib.membershipJSON site=site role=role.memberRole authority=authority>
	"isMemberOfGroup": ${role.memberOfGroup?string},
</@membershipLib.membershipJSON>
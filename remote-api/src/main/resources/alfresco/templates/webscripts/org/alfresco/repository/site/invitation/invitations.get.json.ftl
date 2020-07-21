<#-- list / search / invitations -->

<#import "../../invitation/invitation.lib.ftl" as invitationLib/>
{
	"data": [
    	<#list invitations as invitation>	
    		<@invitationLib.invitationJSON invitation=invitation avatars=avatars />
	   	<#if invitation_has_next>,</#if>
    	</#list>
  	]
}

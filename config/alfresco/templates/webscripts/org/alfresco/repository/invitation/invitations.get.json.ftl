<#-- list / search / invitations -->

<#import "invitation.lib.ftl" as invitationLib/>
{
	"data": [
    	<#list invitations as invitation>	
    		<@invitationLib.invitationJSON invitation=invitation />	 	   
	   	<#if invitation_has_next>,</#if>
    	</#list>
  	]
}

<#-- Get Invitation -->
<#import "../../invitation/invitation.lib.ftl" as invitationLib/>
{
	"data":<@invitationLib.invitationJSON invitation=invitation  avatars=avatars />
}

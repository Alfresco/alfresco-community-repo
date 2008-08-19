<#import "invite.lib.ftl" as inviteLib/>
{
   "invites" : [
      <#list invites as invite>
         <@inviteLib.inviteJSON invite=invite/>
         <#if invite_has_next>,</#if>
      </#list>
   ]
}

<#import "invite.lib.ftl" as inviteLib/>

[
   <#list invites as invite>
      <@inviteLib.inviteJSON invite=invite/>
      <#if invite_has_next>,</#if>
   </#list>
]
<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
<#escape x as jsonUtils.encodeJSONString(x)>
   "${fieldName}":
   {
      <#if person.assocs["cm:avatar"]??>
      "avatar": "${"api/node/" + person.assocs["cm:avatar"][0].nodeRef?string?replace('://','/') + "/content/thumbnails/avatar"}",
      </#if>
      "userName": "${person.properties["cm:userName"]}",
      "firstName": "${person.properties["cm:firstName"]!""}",
      "lastName": "${person.properties["cm:lastName"]!""}"
   },
</#escape>
</#macro>

<#-- renders an invite information object -->
<#macro inviteJSON invite>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "inviteId": "${invite.inviteId!''}",
   <#if invite.inviterPerson??>
      <@renderPerson person=invite.inviterPerson fieldName="inviter" />
   <#else>
   "inviter":
   {
      "userName": "${invite.inviterUserName}"
   },
   </#if>
   <#if invite.inviteePerson??>
      <@renderPerson person=invite.inviteePerson fieldName="invitee" />
   <#else>
   "invitee":
   {
      "userName": "${invite.inviteeUserName}"
   },
   </#if>
   "role": "${invite.role}",
   "site":
   {
      "shortName": "${invite.siteShortName!''}"
      <#if invite.siteInfo??>
      , "title": "${invite.siteInfo.title}"
      </#if>
   },
   "invitationStatus": "${invite.invitationStatus}",
   "sentInviteDate": "${invite.sentInviteDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}"
}
</#escape>
</#macro>

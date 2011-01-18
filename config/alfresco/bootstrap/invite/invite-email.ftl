<#assign inviterPersonRef=args["inviterPersonRef"]/>
<#assign inviterPerson=companyhome.nodeByReference[inviterPersonRef]/>
<#assign inviteePersonRef=args["inviteePersonRef"]/>
<#assign inviteePerson=companyhome.nodeByReference[inviteePersonRef]/>

Hello ${inviteePerson.properties["cm:firstName"]!""},

You have been invited by ${inviterPerson.properties["cm:firstName"]!""} ${inviterPerson.properties["cm:lastName"]!""} to join the '${args["siteName"]}' site.

Your role in the site will be ${args["inviteeSiteRole"]}.

To accept this invitation click the link below.

${args["acceptLink"]}

<#if args["inviteeGenPassword"]?exists>
and enter the following information:

Username: ${args["inviteeUserName"]}
Password: ${args["inviteeGenPassword"]}

We strongly advise you to change your password when you log in for the first time.

</#if>
If you do not want to join the site then click here:

${args["rejectLink"]}

Regards,
Alfresco Share Team
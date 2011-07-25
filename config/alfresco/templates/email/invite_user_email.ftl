${msg("templates.invite_user_email.invited_to_space", space.name, person.properties.firstName)}<#if person.properties.lastName?exists> ${person.properties.lastName}</#if>.

<#if role?exists>${msg("templates.invite_user_email.role", role)}</#if>

${msg("templates.invite_user_email.you_can_view_the_space")}:
<#assign ref=space.nodeRef>
<#assign workspace=ref[0..ref?index_of("://")-1]>
<#assign storenode=ref[ref?index_of("://")+3..]>
${url.serverPath}/alfresco/navigate/browse/${workspace}/${storenode}

${msg("templates.invite_user_email.regards")}

Alfresco

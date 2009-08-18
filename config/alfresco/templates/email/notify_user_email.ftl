A new document '${document.name}', is available in the '${space.name}' space, it was added by ${person.properties.firstName}<#if person.properties.lastName?exists> ${person.properties.lastName}</#if>.

You can view it through this link:
${url.serverPath}/alfresco${document.url}

Or through the Alfresco client:
<#assign ref=space.nodeRef>
<#assign workspace=ref[0..ref?index_of("://")-1]>
<#assign storenode=ref[ref?index_of("://")+3..]>
${url.serverPath}/alfresco/navigate/browse/${workspace}/${storenode}

Regards

Alfresco

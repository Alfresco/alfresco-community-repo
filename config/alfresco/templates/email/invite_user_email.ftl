You have been invited to '${space.name}' by ${person.properties.firstName}<#if person.properties.lastName?exists> ${person.properties.lastName}</#if>.

You will have the role of: ${role}

You can view the space through the Alfresco client:
<#assign ref=space.nodeRef>
<#assign workspace=ref[0..ref?index_of("://")-1]>
<#assign storenode=ref[ref?index_of("://")+3..]>
http://yourserver:8080/alfresco/navigate/browse/${workspace}/${storenode}

Regards

Alfresco

A new document '${document.name}', is available in the '${space.name}' space, it was added by ${person.properties.firstName}
<#if person.properties.lastName?exists>
${person.properties.lastName}
</#if>
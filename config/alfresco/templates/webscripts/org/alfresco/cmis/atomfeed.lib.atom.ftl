<#--                    -->
<#-- ATOM Feed for Node -->
<#--                    -->

<#macro node node>
<author><name>${node.properties.creator!""}</name></author> 
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>urn:uuid:${node.id}</id>
<title>${node.name}</title>
<updated>${xmldate(node.properties.modified)}</updated>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
</#macro>

<#macro generic id title author="System">
<author><name>${author}</name></author>
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>${id}</id>
<title>${title}</title> 
<updated>${xmldate(date)}</updated>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
</#macro>
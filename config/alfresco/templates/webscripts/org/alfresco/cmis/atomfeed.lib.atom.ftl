[#ftl]

[#--            --]
[#-- ATOM Feed  --]
[#--            --]

[#macro feed ns=""]
<feed[#if ns != ""] "${ns}"[/#if]>
[#nested]
</feed>
[/#macro]


[#--                     --]
[#-- ATOM Feed (generic) --]
[#--                     --]

[#macro generic id title author="System"]
<author><name>${author}</name></author>
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>${id}</id>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
[#nested]  [#-- NOTE: Custom links --]
<title>${title}</title> 
<updated>${xmldate(date)}</updated>
[/#macro]


[#--                    --]
[#-- ATOM Feed for Node --]
[#--                    --]

[#macro node node title=""]
<author><name>${node.properties.creator!""}</name></author> 
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>urn:uuid:${node.id}[#if title != ""]-${title}[/#if]</id>
<link rel="self" href="${absurl(encodeuri(url.full))?xml}"/>
<link rel="source" href="${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}"/>
[#nested]  [#-- NOTE: Custom links --]
<title>${node.name}[#if title != ""] ${title?capitalize}[/#if]</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#macro]

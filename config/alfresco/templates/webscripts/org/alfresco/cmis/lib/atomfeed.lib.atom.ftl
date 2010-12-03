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
<author><name>${author?xml}</name></author>
<generator version="${server.version?xml}">Alfresco (${server.edition?xml})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>http://www.alfresco.org/rss/atom/${id}</id>
[#nested]  [#-- NOTE: Custom links --]
<title>${title?xml}</title> 
<updated>${xmldate(date)}</updated>
[/#macro]


[#--                    --]
[#-- ATOM Feed for Node --]
[#--                    --]

[#macro node node kind=""]
<author><name>${node.properties.creator!""}</name></author> 
<generator version="${server.version?xml}">Alfresco (${server.edition?xml})</generator>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<id>urn:uuid:${node.id}[#if kind != ""]-${kind}[/#if]</id>
[#nested]  [#-- NOTE: Custom links --]
<title>${node.name?xml}[#if kind != ""] ${kind?capitalize}[/#if]</title>
<updated>${xmldate(node.properties.modified)}</updated>
[/#macro]

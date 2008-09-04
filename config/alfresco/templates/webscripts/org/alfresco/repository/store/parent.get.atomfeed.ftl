<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/entries.lib.atom.ftl" as entriesLib/>
<?xml version="1.0" encoding="UTF-8"?>
<feed <@nsLib.feedNS/>>
<generator version="${server.version}">Alfresco (${server.edition})</generator>
<title>${node.name}</title> 
<updated>${xmldate(node.properties.modified)}</updated>
<icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<author> 
  <name>${node.properties.creator!""}</name>
</author> 
<id>urn:uuid:${node.id}</id>
<link rel="self" href="${absurl(url.full)?xml}" type="${format.type}"/>
<@parent node=node.parent recurse=returnToRoot/>
</feed>

<#macro parent node recurse=false>
<#if node?exists && node.isContainer>
<entry>
<@entriesLib.folder node=node/>
</entry>
<#if recurse>
   <@parent node=node.parent recurse=true/>
</#if>
</#if>
</#macro>
<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/entries.lib.atom.ftl" as entriesLib/>
<#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/>
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
<@pagingLib.cursor cursor=cursor/>
<#list results as child>
<entry>
<#if child.isDocument>  
<@entriesLib.document node=child/>
<#else>
<@entriesLib.folder node=child/>
</#if>
</entry>
</#list>
</feed>
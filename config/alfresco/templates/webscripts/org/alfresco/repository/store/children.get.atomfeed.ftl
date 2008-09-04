<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/>
<#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/>
<#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/>
<?xml version="1.0" encoding="UTF-8"?>
<feed <@nsLib.feedNS/>>
<@feedLib.node node=node/>
<@pagingLib.cursor cursor=cursor/>
<#list results as child>
<entry>
<#if child.isDocument>  
<@entryLib.document node=child/>
<#else>
<@entryLib.folder node=child/>
</#if>
</entry>
</#list>
</feed>
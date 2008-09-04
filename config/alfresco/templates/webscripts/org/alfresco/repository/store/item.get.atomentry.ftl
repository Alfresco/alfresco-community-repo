<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/>
<#import "/org/alfresco/cmis/cmis.lib.atom.ftl" as cmisLib/>
<?xml version="1.0" encoding="UTF-8"?>
<entry <@nsLib.entryNS/>>
<#if node.isDocument>  
<@entryLib.document node=node/>
<@cmisLib.document node=node/>
<#else>
<@entryLib.folder node=node/>
<@cmisLib.folder node=node/>
</#if>
</entry>

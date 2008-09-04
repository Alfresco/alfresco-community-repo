<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/entries.lib.atom.ftl" as entriesLib/>
<?xml version="1.0" encoding="UTF-8"?>
<entry <@nsLib.entryNS/>>
<#if node.isDocument>  
  <@entriesLib.document node=node/>
<#else>
  <@entriesLib.folder node=node/>
</#if>
</entry>

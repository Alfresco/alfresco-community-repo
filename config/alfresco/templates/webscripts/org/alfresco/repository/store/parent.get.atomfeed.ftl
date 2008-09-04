<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/>
<#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/>
<?xml version="1.0" encoding="UTF-8"?>
<feed <@nsLib.feedNS/>>
<@feedLib.node node=node/>
<@parent node=node.parent recurse=returnToRoot/>
</feed>

<#macro parent node recurse=false>
<#if node?exists && node.isContainer>
<entry>
<@entryLib.folder node=node/>
</entry>
<#if recurse>
   <@parent node=node.parent recurse=true/>
</#if>
</#if>
</#macro>
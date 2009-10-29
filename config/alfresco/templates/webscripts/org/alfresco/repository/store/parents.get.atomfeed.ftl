[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>

[@feedLib.node node "parents"/]
[@parent node.parent returnToRoot/]

</feed>

[/#compress]

[#macro parent node recurse=false]
[#if node?exists && node.isContainer]
  [@entryLib.folder node=node propfilter=filter includeallowableactions=includeAllowableActions includerelationships="none"/]
  [#if recurse && node.id != rootNode.id]
    [@parent node.parent true/]
  [/#if]
[/#if]
[/#macro]

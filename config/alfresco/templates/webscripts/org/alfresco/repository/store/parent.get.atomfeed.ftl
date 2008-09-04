[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>

[@feedLib.node node=node/]
[@parent node=node.parent recurse=returnToRoot/]
[@feedLib.hasMore more="false"/]

</feed>

[/#compress]

[#macro parent node recurse=false]
[#if node?exists && node.isContainer]
  <entry>
    [@entryLib.folder node=node/]
  </entry>
  [#if recurse && node.id != rootNode.id]
    [@parent node=node.parent recurse=true/]
  [/#if]
[/#if]
[/#macro]

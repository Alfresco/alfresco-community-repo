[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node]
  [@pagingLib.links cursor/]
[/@feedLib.node]

[#list results as child]
<entry>
  [#if child.isDocument]
    [@entryLib.document child filter/]
  [#else]
    [@entryLib.folder child filter/]
  [/#if]
</entry>
[/#list]

[@feedLib.hasMore cursor/]
[@pagingLib.opensearch cursor/]

</feed>

[/#compress]
[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node "children"]
  [@pagingLib.links cursor/]
[/@feedLib.node]
[@pagingLib.opensearch cursor/]

[#list results as child]
  [#if child.isDocument]
    [@entryLib.document node=child propfilter=filter includeallowableactions=includeAllowableActions/]
  [#else]
    [@entryLib.folder node=child propfilter=filter includeallowableactions=includeAllowableActions/]
  [/#if]
[/#list]

</feed>

[/#compress]
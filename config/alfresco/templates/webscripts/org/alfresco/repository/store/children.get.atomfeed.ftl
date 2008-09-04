[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node=node]
  [@pagingLib.links cursor=cursor/]
  [@entryLib.folderCMISLinks node=node/]        [#-- TODO: not part of 0.42 schema --]
[/@feedLib.node]

[#list results as child]
<entry>
  [#if child.isDocument]
    [@entryLib.document node=child/]
  [#else]
    [@entryLib.folder node=child/]
  [/#if]
</entry>
[/#list]

[@feedLib.hasMore more=cursor/]
[@entryLib.folderCMISProps node=node/]          [#-- TODO: not part of 0.42 schema --]
[@pagingLib.opensearch cursor=cursor/]

</feed>

[/#compress]
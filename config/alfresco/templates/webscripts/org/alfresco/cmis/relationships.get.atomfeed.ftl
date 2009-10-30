[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node "relationships"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#assign nodeuri][@linksLib.nodeuri node/][/#assign]
  [@linksLib.linkvia href="${nodeuri}"/]
  [@pagingLib.links cursor/]
[/@feedLib.node]
[@pagingLib.opensearch cursor/]
[@pagingLib.cmis cursor/]

[#list results as assoc]
  [@entryLib.assoc assoc=assoc propfilter=filter includeallowableactions=includeAllowableActions/]
[/#list]

</feed>

[/#compress]
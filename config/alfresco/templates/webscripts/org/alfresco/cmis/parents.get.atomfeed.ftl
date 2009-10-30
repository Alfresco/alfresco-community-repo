[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>

[@feedLib.node node "parents"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#assign nodeuri][@linksLib.nodeuri node/][/#assign]
  [@linksLib.linkvia href="${nodeuri}"/]
[/@feedLib.node]

[#if parent?? && parent.isContainer]
  [@entryLib.folder node=parent propfilter=filter includeallowableactions=includeAllowableActions includerelationships="none" relativePathSegment=node.name/]
[/#if]

</feed>

[/#compress]

[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>

[@feedLib.generic "urn:uuid:${node.id}-versions" "Versions of ${node.displayPath}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#assign nodeuri][@linksLib.nodeuri node/][/#assign]
  [@linksLib.linkvia href="${nodeuri}"/]
[/@feedLib.generic]

[#list nodes as version]
  [#if node.isDocument]  
    [@entryLib.document node=version propfilter=filter includeallowableactions=includeAllowableActions/]
  [/#if]
[/#list]

</feed>

[/#compress]
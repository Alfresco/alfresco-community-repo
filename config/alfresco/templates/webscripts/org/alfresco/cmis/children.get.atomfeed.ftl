[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node "children"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#assign nodeuri][@linksLib.nodeuri node/][/#assign]
  [@linksLib.linkvia href="${nodeuri}"/]
  [#if cmisproperty(node, cmisconstants.PROP_PARENT_ID)?is_string]
    [@linksLib.linkchildren node.parent "${cmisconstants.REL_UP}"/]
  [/#if]
  [@linksLib.linkdescendants node/]
  [@linksLib.linktree node/]
  [@pagingLib.links cursor/]
[/@feedLib.node]
[@pagingLib.opensearch cursor/]
[@pagingLib.cmis cursor/]

[#list results as child]
  [#if child.isDocument]
    [@entryLib.document node=child renditionfilter=renditionFilter propfilter=filter includeallowableactions=includeAllowableActions includerelationships=includeRelationships/]
  [#else]
    [@entryLib.folder node=child renditionfilter=renditionFilter propfilter=filter includeallowableactions=includeAllowableActions includerelationships=includeRelationships/]
  [/#if]
[/#list]

</feed>

[/#compress]
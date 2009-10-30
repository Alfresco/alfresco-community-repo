[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#assign title][#if typedef??]Type ${typedef.displayName}[#else]Base Types[/#if][/#assign]
[#assign id][#if typedef??]urn:uuid:types-${typedef.typeId.id}[#else]urn:uuid:types-base[/#if][/#assign]

[@feedLib.generic "${id}" "${title}" "${person.properties.userName}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#if typedef??]
  [#assign typeuri][@linksLib.typeuri typedef/][/#assign]
  [@linksLib.linkvia href="${typeuri}"/]
  [@linksLib.linktypedescendants typedef/]
  [/#if]
  [@pagingLib.links cursor/]
[/@feedLib.generic]
[@pagingLib.opensearch cursor/]
[@pagingLib.cmis cursor/]

[#list results as child]
[@entryLib.typedef child includePropertyDefinitions/]
[/#list]

</feed>

[/#compress]

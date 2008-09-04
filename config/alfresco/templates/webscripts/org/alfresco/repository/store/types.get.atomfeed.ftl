[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#assign title][#if type = "all"]All Types[#else]Type ${type}[/#if][/#assign]
[@feedLib.generic "urn:uuid:types-${type}" "${title}" "${person.properties.userName}"]
  [@pagingLib.links cursor=cursor/]
[/@feedLib.generic]

[#list results as child]
[#-- <entry>  TODO: spec issue 40 --]
  [@entryLib.typedef typedef=child includeProperties=returnPropertyDefinitions/]
[#-- </entry>  TODO: spec issue 40 --]
[/#list]

[@feedLib.hasMore more=cursor/]
[@pagingLib.opensearch cursor=cursor/]

</feed>

[/#compress]

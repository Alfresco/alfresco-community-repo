[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.generic "urn:uuid:checkedout" "Checked out Documents" "${person.properties.userName}"]
  [@pagingLib.links cursor/]
[/@feedLib.generic]

[#list results as child]
  [#if child.isDocument]  
    [@entryLib.pwc node=child propfilter=filter includeallowableactions=includeAllowableActions includerelationships="none"/]
  [/#if]
[/#list]

</feed>

[/#compress]
[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.generic "urn:uuid:changes" "Change Log Entries" "${person.properties.userName}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [@pagingLib.changelinks changeLog/]
[/@feedLib.generic]
[@pagingLib.changeNumItems changeLog/]

[#list changeLog.changeEvents as event]
  [@entryLib.changeentry event=event node=changeLog.changeNodes[event_index] includeacl=includeACL/]
[/#list]

</feed>

[/#compress]
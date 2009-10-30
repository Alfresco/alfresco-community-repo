[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>
[@feedLib.generic "urn:uuid:unfiled" "Unfiled Documents"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
[/@feedLib.generic]
</feed>

[/#compress]
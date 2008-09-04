[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>
  [@feedLib.generic "urn:uuid:unfiled" "Unfiled Documents"/]
</feed>

[/#compress]
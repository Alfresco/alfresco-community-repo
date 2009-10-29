[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<feed [@nsLib.feedNS/]>

[@feedLib.generic "urn:uuid:${node.id}-versions" "Versions of ${node.displayPath}"/]

[#list nodes as version]
  [@entryLib.version version versions[version_index]/]
[/#list]

</feed>

[/#compress]
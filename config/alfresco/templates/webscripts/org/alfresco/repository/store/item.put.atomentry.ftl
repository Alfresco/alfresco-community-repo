[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<entry [@nsLib.entryNS/]>
  [#if node.isDocument]
    [@entryLib.document node=node/]
  [#else]
    [@entryLib.folder node=node/]
  [/#if]
</entry>

[/#compress]
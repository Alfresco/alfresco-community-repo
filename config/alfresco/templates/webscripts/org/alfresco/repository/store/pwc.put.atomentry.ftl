[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

<entry [@nsLib.entryNS/]>
  [#if checkin]
    [@entryLib.document node/]
  [#else]
    [@entryLib.pwc node/]
  [/#if]
</entry>

[/#compress]
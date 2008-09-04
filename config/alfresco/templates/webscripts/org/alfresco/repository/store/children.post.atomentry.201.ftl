[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
[#assign namespace][@nsLib.entryNS/][/#assign]

[#if node.isDocument]
  [@entryLib.document node=node ns=namespace/]
[#else]
  [@entryLib.folder node=node ns=namespace/]
[/#if]

[/#compress]
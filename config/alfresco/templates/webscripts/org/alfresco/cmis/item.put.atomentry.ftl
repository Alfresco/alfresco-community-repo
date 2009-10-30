[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
[#assign namespace][@nsLib.entryNS/][/#assign]

[#if node.isDocument]
  [@entryLib.document node=node includeallowableactions=true includerelationships="none" ns=namespace/]
[#else]
  [@entryLib.folder node=node includeallowableactions=true includerelationships="none" ns=namespace/]
[/#if]

[/#compress]
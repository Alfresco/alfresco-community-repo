[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
[#assign namespace][@nsLib.entryNS/][/#assign]

[#if checkin]
  [@entryLib.document node=node includeallowableactions=true includerelationships="none" ns=namespace/]
[#else]
  [@entryLib.pwc node=node includeallowableactions=true includerelationships="none" ns=namespace/]
[/#if]

[/#compress]
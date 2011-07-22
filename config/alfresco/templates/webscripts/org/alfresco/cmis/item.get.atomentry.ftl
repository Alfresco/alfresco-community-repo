[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
[#assign namespace][@nsLib.entryNS/][/#assign]

[#if assoc?exists]
  [@entryLib.assoc assoc=assoc propfilter=filter includeallowableactions=includeAllowableActions ns=namespace/]
[#elseif node?exists && node.isDocument]
  [@entryLib.document node=node renditionfilter=renditionFilter propfilter=filter includeallowableactions=includeAllowableActions includerelationships=includeRelationships includeacl=includeACL ns=namespace/]
[#else]
  [@entryLib.folder node=node renditionfilter=renditionFilter propfilter=filter includeallowableactions=includeAllowableActions includerelationships=includeRelationships includeacl=includeACL ns=namespace/]
[/#if]

[/#compress]
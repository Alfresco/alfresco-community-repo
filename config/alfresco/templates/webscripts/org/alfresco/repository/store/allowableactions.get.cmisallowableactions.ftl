[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
[#assign namespace][@nsLib.allowableactionsNS/][/#assign]
[@entryLib.allowableactions node=node ns=namespace]
  <cmis:parentId>${cmisproperty(node, "ObjectId")}</cmis:parentId>
  <cmis:parentUrl>${absurl(url.serviceContext)}/api/node/${node.nodeRef.storeRef.protocol}/${node.nodeRef.storeRef.identifier}/${node.nodeRef.id}</cmis:parentUrl>
[/@entryLib.allowableactions]

[/#compress]

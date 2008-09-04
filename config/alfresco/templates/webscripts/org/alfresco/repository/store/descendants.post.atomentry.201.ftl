[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>

[#if node.isDocument]  
  [@entryLib.document node ns=[@nsLib.entryNS/]/]
[#else]
  [@entryLib.folder node ns=[@nsLib.entryNS/]/]
[/#if]

[/#compress]
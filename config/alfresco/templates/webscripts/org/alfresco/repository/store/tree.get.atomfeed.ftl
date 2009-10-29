[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node "tree"/]

[#if depth &gt; 0 || depth == -1]
[#list cmischildren(node, "folders") as child]
  [@entryLib.foldertree node=child propfilter=propFilter includeallowableactions=includeAllowableActions includerelationships=false maxdepth=depth/]
[/#list]
[/#if]

</feed>

[/#compress]

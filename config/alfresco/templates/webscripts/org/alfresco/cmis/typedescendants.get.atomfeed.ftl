[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#if depth &gt; 0 || depth == -1]

[#if typedef??]

[@feedLib.generic "urn:uuid:type-${typedef.typeId.id}-descendants" "Type ${typedef.displayName} Descendants" "${person.properties.userName}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [#assign typeuri][@linksLib.typeuri typedef/][/#assign]
  [@linksLib.linkvia href="${typeuri}"/]
  [@linksLib.linktypechildren typedef/]
[/@feedLib.generic]
[#list typedef.getSubTypes(false) as child]
  [@entryLib.typedef typedefn=child includeProperties=includePropertyDefinitions depth=1 maxdepth=depth/]
[/#list]

[#else]

[@feedLib.generic "urn:uuid:types-all" "All Types" "${person.properties.userName}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
[/@feedLib.generic]
[#list basetypes as child]
  [@entryLib.typedef typedefn=child includeProperties=includePropertyDefinitions depth=1 maxdepth=depth/]
[/#list]

[/#if]
[/#if]

</feed>

[/#compress]
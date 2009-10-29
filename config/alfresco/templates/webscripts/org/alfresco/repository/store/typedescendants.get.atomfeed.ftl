[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#if depth &gt; 0 || depth == -1]

[#if typedef??]
[@feedLib.typedef typedefn=typedef kind="descendants" author="${person.properties.userName}"/]
[#list typedef.getSubTypes(false) as child]
  [@entryLib.typedef typedefn=child includeProperties=includePropertyDefinitions depth=1 maxdepth=depth/]
[/#list]
[#else]
[@feedLib.generic "urn:uuid:types-all" "All Types" "${person.properties.userName}"/]
[#list basetypes as child]
  [@entryLib.typedef typedefn=child includeProperties=includePropertyDefinitions depth=1 maxdepth=depth/]
[/#list]
[/#if]

[/#if]

</feed>

[/#compress]

<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/>
<#import "/org/alfresco/cmis/cmis.lib.atom.ftl" as cmisLib/>
<?xml version="1.0" encoding="UTF-8"?>
<entry <@nsLib.entryNS/>>
<@entryLib.pwc node=pwc/>
<@cmisLib.document node=pwc/>
</entry>

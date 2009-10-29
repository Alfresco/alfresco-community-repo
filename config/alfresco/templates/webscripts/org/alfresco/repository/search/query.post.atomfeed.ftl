[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#-- TODO: uuid --]
[@feedLib.generic "urn:uuid:resultset" "Result set for ${statement}" "${person.properties.userName}"]
  [@pagingLib.links cursor/]
[/@feedLib.generic]
[@pagingLib.opensearch cursor/]

[#assign rs = cmisresultset(resultset)]
[#list rs.rows as row]
[@entryLib.row row=row includeallowableactions=includeAllowableActions/]
[/#list]

</feed>

[/#compress]

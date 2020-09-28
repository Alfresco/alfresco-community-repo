<#import "comment.lib.ftl" as commentLib>
<#import "../generic-paged-results.lib.ftl" as gen>
{
   "nodePermissions":
   {
   <#if node.isLocked || node.hasAspect("cm:workingcopy")>
      "create": false,
      "edit": false,
      "delete": false
   <#else>
      "create": ${node.hasPermission("CreateChildren")?string},
      "edit": ${node.hasPermission("Write")?string},
      "delete": ${node.hasPermission("Delete")?string}
   </#if>
   },
<@gen.pagedResults data=data ; item>
   <@commentLib.commentJSON item=item parent=node />
</@gen.pagedResults>
}
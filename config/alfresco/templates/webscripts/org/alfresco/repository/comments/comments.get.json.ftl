<#import "comment.lib.ftl" as commentLib/>
<#import "../generic-paged-results.lib.ftl" as gen/>
{
   "nodePermissions" : {
      "create" : ${node.hasPermission("CreateChildren")?string},
      "edit" : ${node.hasPermission("Write")?string},
      "delete" : ${node.hasPermission("Delete")?string}
   },
<@gen.pagedResults data=data ; item>
	<@commentLib.commentJSON item=item />
</@gen.pagedResults>
}
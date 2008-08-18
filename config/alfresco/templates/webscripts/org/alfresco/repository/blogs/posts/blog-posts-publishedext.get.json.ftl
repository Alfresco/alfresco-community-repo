<#import "../blogpost.lib.ftl" as blogpostLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
{
   "blogPermissions" : {
      "create" : ${blog.hasPermission("CreateChildren")?string},
      "edit" : ${blog.hasPermission("Write")?string},
      "delete" : ${blog.hasPermission("Delete")?string}
   },
<@gen.pagedResults data=data ; item>
	<@blogpostLib.blogpostJSON item=item />
</@gen.pagedResults>
}

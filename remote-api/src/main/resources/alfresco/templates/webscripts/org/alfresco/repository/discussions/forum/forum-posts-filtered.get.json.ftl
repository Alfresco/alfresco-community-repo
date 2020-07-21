<#import "../post.lib.ftl" as postLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
{
   "forumPermissions":
   {
      <#if forum??>
         "create": ${forum.hasPermission("CreateChildren")?string},
         "edit": ${forum.hasPermission("Write")?string},
         "delete": ${forum.hasPermission("Delete")?string}
      <#else>
         "create": false,
         "edit": false,
         "delete": false
      </#if>      
   },
<@gen.pagedResults data=data ; item>
	<@postLib.postJSON postData=item />
</@gen.pagedResults>
}
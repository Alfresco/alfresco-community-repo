<#import "../post.lib.ftl" as postLib/>
<#import "../../generic-paged-results.lib.ftl" as gen/>
{
   "forumPermissions":
   {
      <#if forum.getParent()?? && forum.getTypeShort() != "st:site" >
        "create": ${(forum.getParent()).hasPermission("CreateChildren")?string},
        "edit": ${(forum.getParent()).hasPermission("Write")?string},
        "delete": ${(forum.getParent()).hasPermission("Delete")?string}
      <#else>
        "create": ${forum.hasPermission("CreateChildren")?string},
        "edit": ${forum.hasPermission("Write")?string},
        "delete": ${forum.hasPermission("Delete")?string}
      </#if>
   },
<@gen.pagedResults data=data ; item>
	<@postLib.postJSON postData=item />
</@gen.pagedResults>
}

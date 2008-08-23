<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#assign paging = doclist.paging>
<#assign user = doclist.user>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${paging.totalRecords?c},
   "startIndex": ${paging.startIndex?c},
   "metadata":
   {
      "permissions":
      {
         "userRole": "${user.role!""}",
         "userAccess":
         {
            "create" : ${user.permissions.create?string},
            "edit" : ${user.permissions.edit?string},
            "delete" : ${user.permissions.delete?string}
         }
      }
   },
   "items":
   [
   <#list doclist.items as item>
      <#assign d = item.asset>
      <#assign version = "1.0">
      <#if d.hasAspect("cm:versionable") && d.versionHistory?size != 0><#assign version = d.versionHistory?sort_by("versionLabel")?reverse[0].versionLabel></#if>
      <#if item.owner?exists>
         <#assign lockedBy = (item.owner.properties.firstName + " " + item.owner.properties.lastName)?trim>
         <#assign lockedByUser = item.owner.properties.userName>
      <#else>
         <#assign lockedBy="" lockedByUser="">
      </#if>
      <#if item.createdBy?exists>
         <#assign createdBy = (item.createdBy.properties.firstName + " " + item.createdBy.properties.lastName)?trim>
         <#assign createdByUser = item.createdBy.properties.userName>
      <#else>
         <#assign createdBy="" createdByUser="">
      </#if>
      <#if item.modifiedBy?exists>
         <#assign modifiedBy = (item.modifiedBy.properties.firstName + " " + item.modifiedBy.properties.lastName)?trim>
         <#assign modifiedByUser = item.modifiedBy.properties.userName>
      <#else>
         <#assign modifiedBy="" modifiedByUser="">
      </#if>
      <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
      {
         "index": ${item_index},
         "nodeRef": "${d.nodeRef}",
         "type": "<#if d.isContainer>folder<#else>document</#if>",
         "mimetype": "${d.mimetype!""}",
         "icon32": "${d.icon32}",
         "fileName": "${d.name}",
         "displayName": "${d.name?replace(workingCopyLabel, "")}",
         "status": "<#list item.status as s>${s}<#if s_has_next>,</#if></#list>",
         "lockedBy": "${lockedBy}",
         "lockedByUser": "${lockedByUser}",
         "title": "${(d.properties.title!"")}",
         "description": "${(d.properties.description!"")}",
         "createdOn": "${d.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
         "createdBy": "${createdBy}",
         "createdByUser": "${createdByUser}",
         "modifiedOn": "${d.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
         "modifiedBy": "${modifiedBy}",
         "modifiedByUser": "${modifiedByUser}",
         "size": "${d.size?c}",
         "version": "${version}",
         "contentUrl": "api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}",
         "actionSet": "${item.actionSet}",
         "tags": <#noescape>[${tags}]</#noescape>,
         "activeWorkflows": "<#list item.activeWorkflows as aw>${aw}<#if aw_has_next>,</#if></#list>",
         "location":
         {
            "site": "${item.location.site!""}",
            "container": "${item.location.container!""}",
            "path": "${item.location.path!""}"
         },
      	"permissions":
      	{
      	   "inherited": ${d.inheritsPermissions?string},
      	   "roles":
      	   [
            <#list d.permissions as permission>
               "${permission?string}"<#if permission_has_next>,</#if>
            </#list>
            ],
      	   "userAccess":
      	   {
               "create": ${d.hasPermission("CreateChildren")?string},
               "edit": ${d.hasPermission("Write")?string},
               "delete": ${d.hasPermission("Delete")?string},
               "permissions": ${d.hasPermission("ChangePermissions")?string}
      	   }
      	}
      }<#if item_has_next>,</#if>
   </#list>
   ]
}
</#escape>

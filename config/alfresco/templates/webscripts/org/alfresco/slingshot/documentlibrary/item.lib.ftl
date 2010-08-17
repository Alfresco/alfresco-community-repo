<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>

<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>

<#macro itemJSON item>
   <#escape x as jsonUtils.encodeJSONString(x)>
      <#assign node = item.node>
      <#assign version = "1.0">
      <#if node.hasAspect("cm:versionable") && node.versionHistory?size != 0><#assign version = node.versionHistory[0].versionLabel></#if>
      <#if item.createdBy??>
         <#assign createdBy = item.createdBy.displayName>
         <#assign createdByUser = item.createdBy.userName>
      <#else>
         <#assign createdBy="" createdByUser="">
      </#if>
      <#if item.modifiedBy??>
         <#assign modifiedBy = item.modifiedBy.displayName>
         <#assign modifiedByUser = item.modifiedBy.userName>
      <#else>
         <#assign modifiedBy="" modifiedByUser="">
      </#if>
      <#if item.lockedBy??>
         <#assign lockedBy = item.lockedBy.displayName>
         <#assign lockedByUser = item.lockedBy.userName>
      <#else>
         <#assign lockedBy="" lockedByUser="">
      </#if>
      <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
   "nodeRef": "${node.nodeRef}",
   "nodeType": "${shortQName(node.type)}",
   "type": "${item.type}",
   "mimetype": "${node.mimetype!""}",
   "isFolder": ${node.isContainer?string},
<#if item.isLink??>
   "isLink": ${item.isLink?string},
   "fileName": "<#if item.isLink>${item.linkNode.name}<#else>${node.name}</#if>",
</#if>
   "displayName": "${node.name?replace(workingCopyLabel, "")}",
   "status": "<#list item.status?keys as s><#if item.status[s]?is_boolean && item.status[s] == true>${s}<#if s_has_next>,</#if></#if></#list>",
   "title": "${node.properties.title!""}",
   "description": "${node.properties.description!""}",
   "author": "${node.properties.author!""}",
   "createdOn": "<@dateFormat node.properties.created />",
   "createdBy": "${createdBy}",
   "createdByUser": "${createdByUser}",
   "modifiedOn": "<@dateFormat node.properties.modified />",
   "modifiedBy": "${modifiedBy}",
   "modifiedByUser": "${modifiedByUser}",
   "lockedBy": "${lockedBy}",
   "lockedByUser": "${lockedByUser}",
   "size": "${node.size?c}",
   "version": "${version}",
   "contentUrl": "api/node/content/${node.storeType}/${node.storeId}/${node.id}/${node.name?url}",
   "webdavUrl": "${node.webdavUrl}",
   "actionSet": "${item.actionSet}",
   "tags": <#noescape>[${tags}]</#noescape>,
   "categories": [<#list node.properties.categories![] as c>["${c.name}", "${c.displayPath?replace("/categories/General","")}"]<#if c_has_next>,</#if></#list>],
   <#if item.activeWorkflows??>"activeWorkflows": "<#list item.activeWorkflows as aw>${aw}<#if aw_has_next>,</#if></#list>",</#if>
   <#if item.isFavourite??>"isFavourite": ${item.isFavourite?string},</#if>
   "location":
   {
      "site": "${item.location.site!""}",
      "siteTitle": "${item.location.siteTitle!""}",
      "container": "${item.location.container!""}",
      "path": "${item.location.path!""}",
      "file": "${item.location.file!""}",
      "parent":
      {
      <#if item.location.parent??>
         <#if item.location.parent.nodeRef??>
         "nodeRef": "${item.location.parent.nodeRef!""}"
         </#if>
      </#if>      
      }
   },
   "permissions":
   {
      "inherited": ${node.inheritsPermissions?string},
      "roles":
      [
      <#list node.fullPermissions as permission>
         "${permission?string}"<#if permission_has_next>,</#if>
      </#list>
      ],
      "userAccess":
      {
      <#list item.actionPermissions?keys as actionPerm>
         <#if item.actionPermissions[actionPerm]?is_boolean>
         "${actionPerm?string}": ${item.actionPermissions[actionPerm]?string}<#if actionPerm_has_next>,</#if>
         </#if>
      </#list>
      }
   },
   <#if item.custom??>"custom": <#noescape>${item.custom}</#noescape>,</#if>
   "actionLabels":
   {
<#if item.actionLabels??>
   <#list item.actionLabels?keys as actionLabel>
      "${actionLabel?string}": "${item.actionLabels[actionLabel]}"<#if actionLabel_has_next>,</#if>
   </#list>
</#if>
   }
   </#escape>
</#macro>

<#macro itemJSON item>
   <#local node = item.node>
   <#local version = "1.0">
   <#if node.hasAspect("{http://www.alfresco.org/model/content/1.0}versionable")><#local version = node.properties["cm:versionLabel"]!""></#if>
   <#escape x as jsonUtils.encodeJSONString(x)>
   "version": "${version}",
   "webdavUrl": "${node.webdavUrl}",
   <#if item.activeWorkflows?? && (item.activeWorkflows?size > 0)>"activeWorkflows": ${item.activeWorkflows?size?c},</#if>
   <#if item.isFavourite??>"isFavourite": ${item.isFavourite?string},</#if>
   <#if (item.workingCopyJSON?length > 2)>"workingCopy": <#noescape>${item.workingCopyJSON}</#noescape>,</#if>
   <#if item.likes??>"likes":
   {
      "isLiked": ${item.likes.isLiked?string},
      "totalLikes": ${item.likes.totalLikes?c}
   }</#if>,
   "location":
   {
      "repositoryId": "${(node.properties["trx:repositoryId"])!(server.id)}",
   <#if item.location.site??>
      "site":
      {
         "name": "${(item.location.site)!""}",
         "title": "${(item.location.siteTitle)!""}",
         "preset": "${(item.location.sitePreset)!""}"
      },
   </#if>
   <#if item.location.container??>
      "container":
      {
         "name": "${(item.location.container)!""}",
         "type": "${(item.location.containerType)!""}",
         "nodeRef": "${(item.location.containerNode.nodeRef)!""}"
      },
   </#if>
      "path": "${(item.location.path)!""}",
      "file": "${(item.location.file)!""}",
      "parent":
      {
      <#if (item.location.parent.nodeRef)??>
         "nodeRef": "${item.location.parent.nodeRef}"
      </#if>
      }
   }
   </#escape>
</#macro>
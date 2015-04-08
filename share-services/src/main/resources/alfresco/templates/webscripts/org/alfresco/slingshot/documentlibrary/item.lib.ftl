<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>

<#-- This function is used below to detect numerical property values of Infinity, -Infinity and NaN -->
<#macro renderNumber value=0>
<#if value?is_number>
   <#if value?c == '\xfffd' || value?c == '\x221e' || value?c == '-\x221e'>0
   <#else>${value?c}
   </#if>
</#if>
</#macro>

<#macro dateFormat date=""><#if date?is_date>${xmldate(date)}</#if></#macro>

<#macro itemJSON item>
   <#escape x as jsonUtils.encodeJSONString(x)>
      <#local node = item.node>
      <#local version = "1.0">
      <#if node.hasAspect("{http://www.alfresco.org/model/content/1.0}versionable")><#local version = node.properties["cm:versionLabel"]!""></#if>
      <#if item.createdBy??>
         <#local createdBy = item.createdBy.displayName>
         <#local createdByUser = item.createdBy.userName>
      <#else>
         <#local createdBy="" createdByUser="">
      </#if>
      <#if item.modifiedBy??>
         <#local modifiedBy = item.modifiedBy.displayName>
         <#local modifiedByUser = item.modifiedBy.userName>
      <#else>
         <#local modifiedBy="" modifiedByUser="">
      </#if>
      <#if item.lockedBy??>
         <#local lockedBy = item.lockedBy.displayName>
         <#local lockedByUser = item.lockedBy.userName>
      <#else>
         <#local lockedBy="" lockedByUser="">
      </#if>
      <#local tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#local>
   "nodeRef": "${node.nodeRef}",
   "nodeType": "${shortQName(node.type)}",
   "type": "${item.type}",
   "mimetype": "${node.mimetype!""}",
   "isFolder": <#if item.linkedNode??>${item.linkedNode.isContainer?string}<#else>${node.isContainer?string}</#if>,
   "isLink": ${(item.isLink!false)?string},
<#if item.linkedNode??>
   "linkedNodeRef": "${item.linkedNode.nodeRef?string}",
</#if>
   "fileName": "<#if item.linkedNode??>${item.linkedNode.name}<#else>${node.name}</#if>",
   "displayName": "<#if item.linkedNode??>${item.linkedNode.name}<#elseif node.hasAspect("{http://www.alfresco.org/model/content/1.0}workingcopy")>${node.name?replace(workingCopyLabel, "")}<#else>${node.name}</#if>",
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
   <#if node.hasAspect("{http://www.alfresco.org/model/content/1.0}thumbnailModification")>
      <#list node.properties.lastThumbnailModification as thumbnailMod>
         <#if thumbnailMod?contains("doclib")>
   "lastThumbnailModification": "${thumbnailMod}",
         </#if>
      </#list>
   </#if>
   "lockedBy": "${lockedBy}",
   "lockedByUser": "${lockedByUser}",
   "size": "${node.size?c}",
   "version": "${version}",
   "contentUrl": "api/node/content/${node.storeType}/${node.storeId}/${node.id}/${node.name?url}",
   "webdavUrl": "${node.webdavUrl}",
   "actionSet": "${item.actionSet}",
   "tags": <#noescape>[${tags}]</#noescape>,
   <#if node.hasAspect("{http://www.alfresco.org/model/content/1.0}generalclassifiable")>
   "categories": [<#list node.properties.categories![] as c>["${c.name}", "${c.displayPath?replace("/categories/General","")}"]<#if c_has_next>,</#if></#list>],
   </#if>
   <#if item.activeWorkflows??>"activeWorkflows": "<#list item.activeWorkflows as aw>${aw}<#if aw_has_next>,</#if></#list>",</#if>
   <#if item.isFavourite??>"isFavourite": ${item.isFavourite?string},</#if>
   "likes":<#if item.likes??>
   {
      "isLiked": ${item.likes.isLiked?string},
      "totalLikes": ${item.likes.totalLikes?c}
   }<#else>null</#if>,
   "location":
   {
      "repositoryId": "${(node.properties["trx:repositoryId"])!(server.id)}",
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
   <#if node.hasAspect("{http://www.alfresco.org/model/content/1.0}geographic")>"geolocation":
   {
      "latitude": <@renderNumber node.properties["cm:latitude"] />,
      "longitude": <@renderNumber node.properties["cm:longitude"] />
   },</#if>
   <#if node.hasAspect("{http://www.alfresco.org/model/audio/1.0}audio")>"audio":
   {
      "album": "${node.properties["audio:album"]!""}",
      "artist": "${node.properties["audio:artist"]!""}",
      "composer": "${node.properties["audio:composer"]!""}",
      "engineer": "${node.properties["audio:engineer"]!""}",
      "genre": "${node.properties["audio:genre"]!""}",
      "trackNumber": <@renderNumber node.properties["audio:trackNumber"] />,
      "releaseDate": "<@dateFormat node.properties["audio:releaseDate"] />",
      "sampleRate": <@renderNumber node.properties["audio:sampleRate"] />,
      "sampleType": "${node.properties["audio:sampleType"]!""}",
      "channelType": "${node.properties["audio:channelType"]!""}",
      "compressor": "${node.properties["audio:compressor"]!""}"
   },</#if>
   <#if node.hasAspect("{http://www.alfresco.org/model/exif/1.0}exif")>"exif":
   {
      "dateTimeOriginal": "<@dateFormat node.properties["exif:dateTimeOriginal"] />",
      "pixelXDimension": <@renderNumber node.properties["exif:pixelXDimension"] />,
      "pixelYDimension": <@renderNumber node.properties["exif:pixelYDimension"] />,
      "exposureTime": <@renderNumber node.properties["exif:exposureTime"] />,
      "fNumber": <@renderNumber node.properties["exif:fNumber"] />,
      "flash": ${(node.properties["exif:flash"]!false)?string},
      "focalLength": <@renderNumber node.properties["exif:focalLength"] />,
      "isoSpeedRatings": "${node.properties["exif:isoSpeedRatings"]!""}",
      "manufacturer": "${node.properties["exif:manufacturer"]!""}",
      "model": "${node.properties["exif:model"]!""}",
      "software": "${node.properties["exif:software"]!""}",
      "orientation": <@renderNumber node.properties["exif:orientation"] />,
      "xResolution": <@renderNumber node.properties["exif:xResolution"] />,
      "yResolution": <@renderNumber node.properties["exif:yResolution"] />,
      "resolutionUnit": "${node.properties["exif:resolutionUnit"]!""}"
   },</#if>
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

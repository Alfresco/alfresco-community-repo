<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
{
   "doclist":
   {
      "totalItems": ${doclist.items?size},
      "items":
      [
      <#list doclist.items as item>
         <#assign d = item.asset>
         <#assign version = "1.0">
         <#if d.hasAspect("cm:versionable")><#assign version = d.versionHistory?sort_by("versionLabel")?reverse[0].versionLabel></#if>
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
         {
            "index": ${item_index},
            "nodeRef": "${d.nodeRef}",
            "type": "<#if d.isContainer>folder<#else>document</#if>",
            "mimetype": "${d.mimetype!""}",
            "icon32": "${d.icon32}",
            "fileName": "${d.name?html}",
            "displayName": "${d.name?replace(workingCopyLabel, "")?html}",
            "status": "<#list item.status as s>${s}<#if s_has_next>,</#if></#list>",
            "lockedBy": "${lockedBy}",
            "lockedByUser": "${lockedByUser}",
            "title": "${(d.properties.title!"")?html}",
            "description": "${(d.properties.description!"")?html}",
            "createdOn": "${d.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "createdBy": "${createdBy}",
            "createdByUser": "${createdByUser}",
            "modifiedOn": "${d.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "modifiedBy": "${modifiedBy}",
            "modifiedByUser": "${modifiedByUser}",
            "size": "${d.size}",
            "version": "${version}",
            "contentUrl": "api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}",
            "actionSet": "${item.actionSet}"
         }<#if item_has_next>,</#if>
      </#list>
      ]
   }
}
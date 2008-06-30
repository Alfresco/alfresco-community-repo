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
         {
            "index": ${item_index},
            "nodeRef": "${d.nodeRef}",
            "type": "<#if d.isContainer>folder<#else>document</#if>",
            "mimetype": "${d.mimetype!""}",
            "icon32": "${d.icon32}",
            "name": "${d.name?replace(" (Working Copy)", "")?html}",
            "status": "<#list item.status as s>${s}<#if s_has_next>,</#if></#list>",
            "lockedBy": "${item.owner}",
            "title": "${(d.properties.title!"")?html}",
            "description": "${(d.properties.description!"")?html}",
            "createdOn": "${d.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "createdBy": "${d.properties.creator}",
            "modifiedOn": "${d.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "modifiedBy": "${d.properties.modifier}",
            "size": "${d.size}",
            "version": "${version}",
            "contentUrl": "api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}",
            "actionSet": "${item.actionSet}"
         }<#if item_has_next>,</#if>
      </#list>
      ]
   }
}
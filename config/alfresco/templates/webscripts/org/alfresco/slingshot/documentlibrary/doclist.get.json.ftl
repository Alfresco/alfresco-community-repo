{
   "doclist":
   {
<#if doclist.error?exists>
      "error": "${doclist.error}"
<#else>
      "items":
      [
      <#list doclist.items as d>
         <#assign status = []>
         <#assign lockedBy = "">
         <#assign version = "1.0">
         <#if d.isLocked><#assign status = status + ["locked"]><#assign lockedBy = d.properties["cm:lockOwner"]></#if>
         <#if d.hasAspect("cm:workingcopy")><#assign status = status + ["workingcopy"]></#if>
         <#if d.hasAspect("cm:versionable")><#assign version = d.versionHistory?sort_by("versionLabel")?reverse[0].versionLabel></#if>
         {
            "index": ${d_index},
            "nodeRef": "${d.nodeRef}",
            "type": "<#if d.isContainer>folder<#else>document</#if>",
            "mimetype": "${d.mimetype!""}",
            "icon32": "${d.icon32}",
            "name": "${d.name?html}",
            "status": "<#list status as s>${s}<#if s_has_next>,</#if></#list>",
            "lockedBy": "${lockedBy}",
            "title": "${(d.properties.title!"")?html}",
            "description": "${(d.properties.description!"")?html}",
            "createdOn": "${d.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "createdBy": "${d.properties.creator}",
            "modifiedOn": "${d.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
            "modifiedBy": "${d.properties.modifier}",
            "size": "${d.size}",
            "version": "${version}",
            "contentUrl": "/api/node/content/${d.storeType}/${d.storeId}/${d.id}/${d.name?url}"
         }<#if d_has_next>,</#if>
      </#list>
      ]
</#if>
   }
}
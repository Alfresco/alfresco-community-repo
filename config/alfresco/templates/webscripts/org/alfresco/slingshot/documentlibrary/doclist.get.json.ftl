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
         <#assign version = "">
         <#if d.isLocked><#assign status = status + ["locked"]><#assign lockedBy = d.properties["cm:lockOwner"]></#if>
         <#if d.hasAspect("cm:workingcopy")><#assign status = status + ["workingcopy"]></#if>
         <#if d.hasAspect("cm:versionable")><#assign version = d.versionHistory?sort_by("versionLabel")?reverse[0].versionLabel></#if>
         {
            "nodeRef": "${d.nodeRef}",
            "type": "<#if d.isContainer>folder<#else>document</#if>",
            "icon32": "${d.icon32}",
            "name": "${d.name}",
            "status": "<#list status as s>${s}<#if s_has_next>,</#if></#list>",
            "lockedBy": "${lockedBy}",
            "title": "${d.properties.title!""}",
            "description": "${d.properties.description!""}",
            "createdOn": "${d.properties.created?datetime}",
            "createdBy": "${d.properties.creator}",
            "modifiedOn": "${d.properties.modified?datetime}",
            "modifiedBy": "${d.properties.modifier}",
            "size": "${d.size}",
            "version": "${version}"
         }<#if d_has_next>,</#if>
      </#list>
      ]
</#if>
   }
}
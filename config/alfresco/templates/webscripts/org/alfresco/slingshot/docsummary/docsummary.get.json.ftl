{
   <#if docs.error?exists>
      "error": "${docs.error}"
   <#else>
      "items":
      [
      <#list docs.items as d>
         {
            "nodeRef": "${d.nodeRef}",
            "icon16": "${d.icon16}",
            "icon32": "${d.icon32}",
            "name": "${d.name}",
            "title": "${d.properties.title!""}",
            "description": "${d.properties.description!""}",
            "createdOn": "${d.properties.created?datetime}",
            "createdBy": "${d.properties.creator}",
            "modifiedOn": "${d.properties.modified?datetime}",
            "modifiedBy": "${d.properties.modifier}",
            "contentUrl": "/api/node/content/${d.storeType}/${d.storeId}/${d.id}"
         }<#if d_has_next>,</#if>
      </#list>
      ]
   </#if>
}
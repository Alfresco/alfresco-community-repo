<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if docs.error?exists>
      "error": "${docs.error}"
   <#else>
      "items":
      [
      <#list docs.items as item>
         <#assign d = item.asset>
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
            "nodeRef": "${d.nodeRef}",
            "icon16": "${d.icon16}",
            "icon32": "${d.icon32}",
            "name": "${d.name}",
            "title": "${d.properties.title!""}",
            "description": "${d.properties.description!""}",
            "createdOn": <#noescape>"${d.properties.created?datetime}"</#noescape>,
            "createdBy": "${createdBy}",
            "createdByUser": "${createdByUser}",
            "modifiedOn": <#noescape>"${d.properties.modified?datetime}"</#noescape>,
            "modifiedBy": "${modifiedBy}",
            "modifiedByUser": "${modifiedByUser}",
            "contentUrl": "/api/node/content/${d.storeType}/${d.storeId}/${d.id}"
         }<#if item_has_next>,</#if>
      </#list>
      ]
   </#if>
}
</#escape>
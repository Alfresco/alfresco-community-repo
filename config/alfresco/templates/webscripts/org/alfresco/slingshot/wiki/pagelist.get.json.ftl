<#macro dateFormat date>${xmldate(date)}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalPages": ${wiki.pages?size?c},
   "permissions":
   {
      "create": ${wiki.container.hasPermission("CreateChildren")?string}
   },
   "pages":
   [
      <#if pageMetaOnly>
      <#list wiki.pages as p>
      <#assign page = p.page>
      {
         "name": "${p.name}",
         "title": "<#if p.title?has_content>${p.title}<#else>${p.name?replace("_", " ")}</#if>",
      }<#if p_has_next>,</#if>
      </#list>
      <#else>
      <#list wiki.pages?sort_by(['modified'])?reverse as p>
      <#assign node = p.node>
      <#assign page = p.page>
      {
         "name": "${p.name}",
         "title": "<#if p.title?has_content>${p.title}<#else>${p.name?replace("_", " ")}</#if>",
         "text": "${page.contents}",
         "tags": [
            <#list p.tags as tag>
               "${tag}"<#if tag_has_next>,</#if>
            </#list>
         ],
         "createdOn": "<@dateFormat p.created />",
         <#if p.createdBy??>
            <#assign createdBy = (p.createdBy.properties.firstName!"" + " " + p.createdBy.properties.lastName!"")?trim>
            <#assign createdByUser = p.createdBy.properties.userName>
         <#else>
            <#assign createdBy="">
            <#assign createdByUser="">
         </#if>
         "createdBy": "${createdBy}",
         "createdByUser": "${createdByUser}",
         "modifiedOn": "<@dateFormat p.modified />",
         <#if p.modifiedBy??>
            <#assign modifiedBy = (p.modifiedBy.properties.firstName!"" + " " + p.modifiedBy.properties.lastName!"")?trim>
            <#assign modifiedByUser = p.modifiedBy.properties.userName>
         <#else>
            <#assign modifiedBy="">
            <#assign modifiedByUser="">
         </#if>
         "modifiedBy": "${modifiedBy}",
         "modifiedByUser": "${modifiedByUser}",
         "permissions":
         {
            "edit": ${node.hasPermission("Write")?string},
            "delete": ${node.hasPermission("Delete")?string}
         }
      }<#if p_has_next>,</#if>
      </#list>
      </#if>
   ],
   "pageTitles":
   [
      <#if wiki.pageTitles??>
         <#list wiki.pageTitles as title>
            "${title}"<#if title_has_next>,</#if>
         </#list>
      </#if>
   ]
}
</#escape>

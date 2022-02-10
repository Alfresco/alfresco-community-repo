<#macro dateFormat date>${xmldate(date)}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.page??>
   <#assign page = result.page>
   <#assign node = result.node>
   "name": "${page.systemName}",
   "title": "<#if page.title?has_content>${page.title}<#else>${page.systemName?replace("_", " ")}</#if>",
   "pagetext": "${page.contents}",
   "pageList": [
   <#list result.pageList as p>
      "${p}"<#if p_has_next>,</#if>
   </#list>
   ]<#if !result.minWikiData>,</#if>
   <#if !result.minWikiData>
      "tags": [
      <#list result.tags as tag>
         "${tag}"<#if tag_has_next>,</#if>
      </#list>
      ],
      "links": [
      <#list result.links as link>
         "${link}"<#if link_has_next>,</#if>
      </#list>
      ],
      <#if node.hasAspect("cm:versionable")>
      "versionhistory": [
         <#list node.versionHistory as record>
      {
         "name": "${record.name}",
         "title": "${record.title!""}",
         "version": "${record.versionLabel}",
         "versionId": "${record.id}",
         "date": "<@dateFormat record.createdDate />",
         "author": "${record.creator}"
      }<#if record_has_next>,</#if>
         </#list>
      ],
      </#if>
      "permissions":
      {
         "create": ${result.container.hasPermission("CreateChildren")?string},
         "edit": ${node.hasPermission("Write")?string},
         "delete": ${node.hasPermission("Delete")?string}
      }
   </#if>
<#else>
   "error" : "${result.error!""}"
</#if>
}
</#escape>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.page??>
   <#assign page = result.page>
   "title": "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
   "pagetext": "${page.content}",
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
   "pageList": [
   <#list result.pageList as p>
      "${p}"<#if p_has_next>,</#if>
   </#list>
   ],
   <#if page.hasAspect("cm:versionable")>
   "versionhistory": [
      <#list page.versionHistory as record>
   {
      "name": "${record.name}",
      "version": "${record.versionLabel}",
      "versionId": "${record.id}",
      "date": "${record.createdDate?datetime?string("yyyy-mm-dd'T'HH:MM:ss")}",
      "author": "${record.creator}"     
   }<#if record_has_next>,</#if>
      </#list> 
   ],
   </#if>  
   "permissions":
   {
      "create": ${result.container.hasPermission("CreateChildren")?string},
      "edit": ${page.hasPermission("Write")?string},
      "delete": ${page.hasPermission("Delete")?string}
   }
<#else>
   "error" : "${result.error!""}"
</#if>
}
</#escape>
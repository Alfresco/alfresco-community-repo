<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.page??>
<#assign page = result.page>
   "title" : "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
   "pagetext" : "${page.content}",
   "editable" : "<#if page.hasPermission("Write")>true<#else>false</#if>",
   "tags" : [
      <#list result.tags as tag>
         "${tag}"<#if tag_has_next>,</#if>
      </#list>  
    ],
    "links": [
      <#list result.links as link>
      "${link}"<#if link_has_next>,</#if>
      </#list>
    ]
   <#if page.hasAspect("cm:versionable")>
      , "versionhistory" : [
	   <#list page.versionHistory?sort_by("versionLabel")?reverse as record>
	   {
	      "name": "${record.name}",
	      "version": "${record.versionLabel}",
	      "versionId": "${record.id}",
         "date": "${record.createdDate?datetime?string("dd MMMM yyyy HH:mm")}",
	      "author": "${record.creator}"  	
	   }<#if record_has_next>,</#if>
	   </#list> 
	   ]
  </#if>  
<#else>
   "error" : "${result.error!""}"
</#if>
}
</#escape>
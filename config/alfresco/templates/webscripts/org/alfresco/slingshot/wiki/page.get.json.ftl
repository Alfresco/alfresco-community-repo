{
<#if result?exists>
<#assign page = result.page>
   "title" : "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
   "pagetext" : '${page.content?js_string}',
   "editable" : '<#if page.hasPermission("Write")>true<#else>false</#if>',
   "tags" : [
      <#list result.tags as tag>
         "${tag}"<#if tag_has_next>,</#if>
      </#list>  
    ]
   <#if page.hasAspect("cm:versionable")>
      , "versionhistory" : [
	   <#list page.versionHistory?sort_by("versionLabel")?reverse as record>
	   {
	      "version": "${record.versionLabel}",
	      "versionId": "${record.id}",
         "date": "${record.createdDate?datetime}",
	      "author": "${record.creator}"  	
	   }<#if record_has_next>,</#if>
	   </#list> 
	   ]
  </#if>  
<#else>
  "error" : "Could not find page"
</#if>
}
{
<#if page?exists>
  "pagetext" : '${jsonPageText}',
  "editable" : '<#if page.hasPermission("Write")>true<#else>false</#if>'
  <#if page.hasAspect("cm:versionable")>
, "versionhistory" : [
	<#list page.versionHistory?sort_by("versionLabel")?reverse as record>
	{
	  "version": "${record.versionLabel}",
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
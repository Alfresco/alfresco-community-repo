<#macro assetJSON asset depth=1>
     {
         "path" :    <#escape x as jsonUtils.encodeJSONString(x)> "${asset.path}" </#escape>,
         "name" :    <#escape x as jsonUtils.encodeJSONString(x)> "${asset.name}" </#escape>,
         "creator" : <#escape x as jsonUtils.encodeJSONString(x)> "${asset.creator}" </#escape>,
         "createdDate" : { "iso8601" : "${sandbox.createdDateAsISO8601}" },
         "modifier" : <#escape x as jsonUtils.encodeJSONString(x)> "${asset.modifier}" </#escape>,
         "modifiedDate" : { "iso8601" : "${asset.modifiedDateAsISO8601}" },
         "isLocked" : ${asset.locked?string("true", "false")},
         "isFile" : ${asset.file?string("true", "false")},
         "isFolder" : ${asset.folder?string("true", "false")},
         "isDeleted" : ${asset.deleted?string("true", "false")},
         "properties" : {
         <#list asset.properties?keys as id>
		 	<#assign property = asset.properties[id]>
		 	"${id}" : "${property}" <#if id_has_next>,</#if>
         </#list>         
	 	 },	
	 	 <#if (asset.folder) >
	 	 	<#if (depth > 0) >
	 		"children" : [
	     	<#list asset.children as child >
	     		<@assetJSON child depth-1 />
	     		<#if child_has_next>,</#if>
	     	</#list>
	 	 	],
	 	 	</#if>
	 	<#else>
	 	"version" : ${asset.version?c},
	 	"fileSize" : ${asset.fileSize?c}, 
	 	</#if>
	 	"url" : <#escape x as jsonUtils.encodeJSONString(x)>"${url.serviceContext + "/api/wcm/webprojects/" + webproject.webProjectRef + "/sandboxes/" + sandbox.sandboxRef + "/assets" + asset.path}" </#escape>,
	 	"contentURL" : <#escape x as jsonUtils.encodeJSONString(x)>"${url.serviceContext + "/api/path/content/avm/" + sandbox.sandboxRef + asset.path}" </#escape>
	 	
     }
</#macro>

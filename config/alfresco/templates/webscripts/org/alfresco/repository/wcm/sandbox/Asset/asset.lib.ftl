<#macro assetJSON asset depth=1>
<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "path" : "${asset.path}",
      "name" : "${asset.name}",
      "creator" : "${asset.creator}",
      "createdDate" : { "iso8601" : "${sandbox.createdDateAsISO8601}" },
      "modifier" : "${asset.modifier}",
      "modifiedDate" : { "iso8601" : "${asset.modifiedDateAsISO8601}" },
      "isLocked" : ${asset.locked?string("true", "false")},
      "isFile" : ${asset.file?string("true", "false")},
      "isFolder" : ${asset.folder?string("true", "false")},
      "isDeleted" : ${asset.deleted?string("true", "false")},
      "properties" : {
         <#list asset.properties?keys as id>
         <#if (asset.properties[id]??)>
         "${id}" : "${asset.properties[id]}"<#if id_has_next>,</#if>
         </#if>
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
      "url" : "${url.serviceContext + "/api/wcm/webprojects/" + webproject.webProjectRef + "/sandboxes/" + sandbox.sandboxRef + "/assets" + asset.path}",
      "contentURL" : "${url.serviceContext + "/api/path/content/avm/" + sandbox.sandboxRef + asset.path}"
   }
</#escape>
</#macro>
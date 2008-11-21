<#macro assetJSON asset>
      {
         "path" :    <#escape x as jsonUtils.encodeJSONString(x)> "${asset.path}"    </#escape>,
         "name" :    <#escape x as jsonUtils.encodeJSONString(x)> "${asset.name}"    </#escape>,
         "creator" : <#escape x as jsonUtils.encodeJSONString(x)> "${asset.creator}" </#escape>,
         "createdDate" : { "iso8601" : "${sandbox.createdDateAsISO8601}" },
         "modifier" : <#escape x as jsonUtils.encodeJSONString(x)> "${asset.modifier}" </#escape>,
         "isFile" : ${asset.file?string("true", "false")},
         "isDirectory" : ${asset.directory?string("true", "false")},
         "isDeleted" : ${asset.deleted?string("true", "false")}
      }
</#macro>
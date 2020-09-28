{
  "data": {
<#escape x as jsonUtils.encodeJSONString(x)>
   <#list mimetypes?keys as mimetype>
      "${mimetype}": {
         "description": "${mimetypes[mimetype]}",
         "extensions": {
            "default": "${defaultExtensions[mimetype]}",
            "additional": [
               <#if (otherExtensions[mimetype])?has_content>
                  <#list otherExtensions[mimetype] as ext>
                     "${ext}"<#if ext_has_next>,</#if>
                  </#list>
               </#if>
            ]
         }
      }<#if mimetype_has_next>,</#if>
   </#list>
</#escape>
  }
}

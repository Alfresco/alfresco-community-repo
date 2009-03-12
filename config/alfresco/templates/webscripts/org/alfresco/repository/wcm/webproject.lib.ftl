<#macro webprojectJSON webproject>

{
    
   "url" : <#escape x as jsonUtils.encodeJSONString(x)> "${url.serviceContext + "/api/wcm/webprojects/" + webproject.webProjectRef}", </#escape>
   "webprojectref" : <#escape x as jsonUtils.encodeJSONString(x)> "${webproject.webProjectRef}", </#escape>
   "name" : <#escape x as jsonUtils.encodeJSONString(x)> "${webproject.name}", </#escape>
   "title" : <#escape x as jsonUtils.encodeJSONString(x)> "${webproject.title}", </#escape>
   "description" : <#escape x as jsonUtils.encodeJSONString(x)> "${webproject.description}", </#escape>
   "isTemplate" : ${webproject.template?string("true", "false")},
   "node" : <@nodeJSON nodeRef=webproject.nodeRef/>
}

</#macro>

// Render a scriptNode for JSON
<#macro nodeJSON nodeRef>
<#escape y as jsonUtils.encodeJSONString(y)>
{
    "id" : "${nodeRef.id}",
    "nodeRef" : "${nodeRef}"
}
</#escape>
</#macro>


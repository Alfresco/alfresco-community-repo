<#macro webprojectJSON webproject>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "url" : "${url.serviceContext + "/api/wcm/webproject/" + webproject.webProjectRef}",
   "webprojectref" : "${webproject.webProjectRef}",
   "name" : "${webproject.name}",
   "title" : "${webproject.title}",
   "description" : "${webproject.description}"
}
</#escape>
</#macro>


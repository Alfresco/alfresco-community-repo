<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if result.error?exists>
  error : "${result.error}"
<#else>
  name: "${result.name}",
  what: "${result.what}",
  description: "${result.description}",
  location: "${result.location}",
  tags: [
  <#list result.tags as tag>
   "${tag}"<#if tag_has_next>,</#if>
  </#list>
  ],
  from: "${result.from?string("M/d/yyyy")}",
  start: "${result.from?string("HH:mm")}",
  to: "${result.to?string("M/d/yyyy")}",
  end: "${result.to?string("HH:mm")}"
  <#if result.allday?exists>, allday: "true"</#if>
</#if>
}
</#escape>


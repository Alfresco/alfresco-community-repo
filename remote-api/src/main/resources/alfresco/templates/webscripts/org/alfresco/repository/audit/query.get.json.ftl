<#escape x as jsonUtils.encodeJSONString(x)>
{
   "count":${count?c},
   "entries": 
   [
      <#list entries as entry>
      {
         "id":${entry.id?c},
         "application":"${entry.application}",
         "user":<#if entry.user??>"${entry.user}"<#else>null</#if>,
         "time":"${xmldate(entry.time)}",
         "values":
         <#if entry.values??>
         {
             <#assign first=true>
             <#list entry.values?keys as k>
                 <#if entry.values[k]??>
                     <#if !first>,<#else><#assign first=false></#if>"${k}":<#assign value = entry.values[k]>"${value}"
                 </#if>
             </#list>
         }
         <#else>null</#if>
      }<#if entry_has_next>,</#if>
      </#list>
   ]
}
</#escape>
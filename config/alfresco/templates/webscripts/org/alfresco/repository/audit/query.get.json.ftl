{
   "entries": 
   [
      <#list entries as entry>
      {
         "id":${entry.id},
         "application":${entry.application},
         "user":<#if entry.user??>${entry.user}<#else>null</#if>,
         "time":${entry.time?c},
         "values":
         <#if entry.values??>
         {
             <#assign first=true>
             <#list entry.values?keys as k>
                 <#if entry.values[k]??>
                     <#if !first>,<#else><#assign first=false></#if>"${k}":
                     <#assign value = entry.values[k]>"${value}"
                 </#if>
             </#list>
         }
         <#else>null</#if>
      }<#if entry_has_next>,</#if>
      </#list>
   ]
}

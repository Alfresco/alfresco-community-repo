<#macro pagedResults data>
   "total": ${data.total?c},
   "pageSize": ${data.pageSize?c},
   "startIndex": ${data.startIndex?c},
   "itemCount": ${data.itemCount?c},
   "items":
   [
   <#list data.items as item>
      <#nested item>
      <#if item_has_next>,</#if>
   </#list>
   ]
</#macro>
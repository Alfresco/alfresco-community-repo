{
   "doclist":
   {
<#if doclist.error?exists>
      "error": "${doclist.error}"
<#else>
      "items":
      [
      <#list doclist.items as d>
         {
            "nodeRef": "${d.nodeRef}",
            "type": "<#if d.isContainer>folder<#else>document</#if>",
            "icon16": "${d.icon16}",
            "icon32": "${d.icon32}",
            "name": "${d.name}",
            "status": "",
            "description": "${d.description!""}"
         }
         <#if d_has_next>,</#if>
      </#list>
      ]
</#if>
   }
}
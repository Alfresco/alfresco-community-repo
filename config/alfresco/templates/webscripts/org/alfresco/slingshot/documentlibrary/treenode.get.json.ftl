{
   "treenode":
   {
      "totalItems": ${treenode.items?size},
      "items":
      [
<#list treenode.items as t>
   <#assign hasChildren = false>
   <#list t.children as c>
      <#if c.isContainer><#assign hasChildren = true><#break></#if>
   </#list>
         {
            "nodeRef": "${t.nodeRef}",
            "name": "${t.name?html}",
            "description": "${(t.properties.description!"")?html}",
            "hasChildren": ${hasChildren?string}
         }<#if t_has_next>,</#if>
</#list>
      ]
   }
}
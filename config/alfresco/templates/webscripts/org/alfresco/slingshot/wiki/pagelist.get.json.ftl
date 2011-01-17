<#macro dateFormat date>${xmldate(date)}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalPages" : ${wiki.pages?size?c},
   "permissions":
   {
      "create": ${wiki.container.hasPermission("CreateChildren")?string}
   },
   "pages":
   [
   <#list wiki.pages?sort_by(['modified'])?reverse as p>
      <#assign page = p.page>
      {
         "name" : "${page.name}",
         "title" : "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
         <#-- Strip out any HTML tags -->
         "text" : "${page.content}",
         "tags" : [
             <#list p.tags as tag>
                "${tag}"<#if tag_has_next>,</#if>
             </#list>  
           ],
         "createdOn": "<@dateFormat page.properties.created />",
         <#if p.createdBy??>
            <#assign createdBy = (p.createdBy.properties.firstName!"" + " " + p.createdBy.properties.lastName!"")?trim>
            <#assign createdByUser = p.createdBy.properties.userName>
         <#else>
            <#assign createdBy="">
            <#assign createdByUser="">
         </#if>
         "createdBy": "${createdBy}",
         "createdByUser": "${createdByUser}",
         "modifiedOn": "<@dateFormat page.properties.modified />",
         <#if p.modifiedBy??>
            <#assign modifiedBy = (p.modifiedBy.properties.firstName!"" + " " + p.modifiedBy.properties.lastName!"")?trim>
            <#assign modifiedByUser = p.modifiedBy.properties.userName>
         <#else>
            <#assign modifiedBy="">
            <#assign modifiedByUser="">
         </#if>
         "modifiedBy": "${modifiedBy}",
         "modifiedByUser": "${modifiedByUser}",
         "permissions":
         {
            "edit": ${page.hasPermission("Write")?string},
            "delete": ${page.hasPermission("Delete")?string}
         }
      }<#if p_has_next>,</#if>
   </#list>
   ]
}
</#escape>
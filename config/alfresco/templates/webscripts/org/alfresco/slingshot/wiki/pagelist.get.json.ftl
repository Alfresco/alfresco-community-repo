{
   "totalPages" : ${pageList.pages?size},
   "pages":
   [
   <#list pageList.pages?sort_by(['modified'])?reverse as p>
      <#assign page = p.page>
      {
         "name" : "${page.name}",
         "editable" : "<#if page.hasPermission("Write")>true<#else>false</#if>",
         "title" : "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
         <#-- Strip out any HTML tags -->
         "text" : "${page.content?replace("</?[^>]+>", " ", "ir")}",
         "createdOn": "${page.properties.created?string("MMM dd yyyy, HH:mm:ss")}",
         <#if p.createdBy??>
            <#assign createdBy = (p.createdBy.properties.firstName + " " + p.createdBy.properties.lastName)?trim>
         <#else>
            <#assign createdBy="">
         </#if>
         "createdBy": "${createdBy}",
         "modifiedOn": "${page.properties.modified?string("MMM dd yyyy, HH:mm:ss")}",
         <#if p.modifiedBy??>
            <#assign modifiedBy = (p.modifiedBy.properties.firstName + " " + p.modifiedBy.properties.lastName)?trim>
         <#else>
            <#assign modifiedBy="">
         </#if>
         "modifiedBy": "${modifiedBy}"
      }<#if p_has_next>,</#if>
   </#list>
   ]
}
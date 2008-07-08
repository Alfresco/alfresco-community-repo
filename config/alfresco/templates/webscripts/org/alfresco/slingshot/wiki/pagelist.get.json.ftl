{
   "totalPages" : ${pageList.pages?size},
   "pages":
   [
   <#list pageList.pages?sort_by(['properties','modified'])?reverse as page>
      {
         "name" : "${page.name}",
         "editable" : "<#if page.hasPermission("Write")>true<#else>false</#if>",
         "title" : "<#if page.properties.title?exists>${page.properties.title}<#else>${page.name?replace("_", " ")}</#if>",
         <#-- strip out any html tags and/or wiki markup -->
         "text" : "${page.content?replace("</?[^>]+>", " ", "ir")?replace("\\[\\[(?:[a-zA-Z\\s]+\\|)?([^\\]]+)\\]\\]", "$1", "ir")?j_string}",
         "createdOn": "${page.properties.created?string("MMM dd yyyy, HH:mm:ss")}",
         "createdBy": "${page.properties.creator}",
         "modifiedOn": "${page.properties.modified?string("MMM dd yyyy, HH:mm:ss")}",
         "modifiedBy": "${page.properties.modifier}"
      }<#if page_has_next>,</#if>
   </#list>
   ]
}
<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#-- assign queryString = "(TEXT:\"${searchString}\") OR (@cm\\:name:*${searchString}*)"  -->
      <#assign queryString = "@\\{http\\://www.alfresco.org/model/content/1.0\\}name:*${searchString}*" >
   </#if>
<#else>
   <#assign searchString = "">
   <#assign queryString = "">
</#if>

<#if searchString != "">
   <#if args.maxresults?exists>
      <#assign maxresults=args.maxresults?number>
   <#else>
      <#assign maxresults=10>
   </#if>

   <#assign rescount=0>

   <table>
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
      <tr>
         <td>(No results found)</td>
      </tr>
   <#else>
      <#assign totalResults = results?size>
      <#list results as child>
         <#assign rescount=rescount + 1>
         <#if child.isDocument>
            <#if child.name?ends_with(".pdf")>
               <#assign openURL = "${url.context}${child.url}">
               <#assign hrefExtra = " target=\"_blank\"">
            <#else>
               <#assign webdavPath = (child.displayPath?substring(13) + '/' + child.name)?url('ISO-8859-1')?replace('%2F', '/')?replace('\'', '\\\'') />
               <#assign openURL = "#">
               <#assign hrefExtra = " onClick=\"window.external.openDocument('${webdavPath}')\"">
            </#if>
         <#else>
            <#assign openURL = "${url.serviceContext}/office/navigation?p=${args.p?url}&amp;n=${child.id}&amp;search=${searchString?url}&amp;maxresults=${maxresults}">
            <#assign hrefExtra = "">
         </#if>
      <tr>
         <td style="width: 32px;">
            <a href="${openURL}" ${hrefExtra}><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
         </td>
         <td>
            <a href="${openURL}" ${hrefExtra} title="Open ${child.name}">${child.name}</a><br/>
         <#if child.properties.description?exists>
            ${child.properties.description}<br/>
         </#if>
         <#if child.isDocument>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br/>
         </#if>
         </td>
      </tr>
         <#if rescount = maxresults>
            <#break>
         </#if>
      </#list>
   </#if>
   </table>
</#if>
<script type="text/javascript">
   OfficeSearch.itemsFound(${rescount}, ${totalResults});
</script>
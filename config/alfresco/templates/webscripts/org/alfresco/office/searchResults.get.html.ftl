<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#assign queryString = "TEXT:\"${searchString}\" @cm\\:title:${searchString}">
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

   <#assign rescount=1>

   <table>
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
      <tr>
         <td>(No results found)</td>
      </tr>
   <#else>
      <#list results as child>
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
            <#assign openURL = "${url.context}/service/office/navigation?p=${args.p}&n=${child.id}&search=${searchString}&maxresults=${maxresults}">
            <#assign hrefExtra = "">
         </#if>
      <tr>
         <td width="32">
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
       <#assign rescount=rescount + 1>
      </#list>
   </#if>
   </table>
</#if>

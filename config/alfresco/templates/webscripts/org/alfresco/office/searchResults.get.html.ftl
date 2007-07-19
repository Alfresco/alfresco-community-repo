<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if>
<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#assign queryString = "(TEXT:\"${searchString}\") OR (@cm\\:name:*${searchString}*)" >
      <#-- assign queryString = "@\\{http\\://www.alfresco.org/model/content/1.0\\}name:*${searchString}*" -->
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
   <#assign resCount=0>
   <#assign totalResults=0>
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
   <div class="noItems">
      <span>(No results found)</span>
   </div>
   <#else>
      <#assign totalResults = results?size>
      <#list results as child>
         <#assign resCount=resCount + 1>
         <#if child.isDocument>
            <#if child.name?ends_with(extn)>
               <#assign relativePath = (child.displayPath?substring(companyhome.name?length+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
               <#assign openURL = "#">
               <#assign hrefExtra = " onClick=\"window.external.openDocument('${relativePath}')\"">
            <#else>
               <#assign openURL = "${url.context}${child.url}?ticket=${session.ticket}">
               <#assign hrefExtra = " target=\"_blank\"">
            </#if>
         <#else>
            <#assign openURL = "${url.serviceContext}/office/navigation?p=${args.p?url}&amp;e=$(extn}&amp;n=${child.id}&amp;search=${searchString?url}&amp;maxresults=${maxresults}">
            <#assign hrefExtra = "">
         </#if>
   <div class="documentItem ${(resCount % 2 = 0)?string("odd", "even")}"">
      <span class="documentItemIcon">
         <a href="${openURL}" ${hrefExtra}><img src="${url.context}${child.icon32}" alt="Open ${child.name}" /></a>
      </span>
      <span class="documentItemDetails">
         <a class="bold" href="${openURL}" ${hrefExtra} title="Open ${child.name}">${child.name}</a><br />
         <#if child.properties.description?exists>
            <#if (child.properties.description?length > 0)>
               ${child.properties.description}<br />
            </#if>
         </#if>
         <#if child.isDocument>
            Modified: ${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)<br />
         </#if>
      </span>
   </div>
         <#if resCount = maxresults>
            <#break>
         </#if>
      </#list>
   </#if>
</#if>
<script type="text/javascript">
   OfficeSearch.itemsFound(${resCount}, ${totalResults});
</script>
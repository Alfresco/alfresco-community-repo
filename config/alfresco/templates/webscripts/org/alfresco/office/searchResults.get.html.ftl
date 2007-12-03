<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
<#if args.search?exists><#assign searchString = args.search><#else><#assign searchString=""></#if>

<#if args.maxresults?exists>
   <#assign maxresults=args.maxresults?number>
<#else>
   <#assign maxresults=10>
</#if>
<#assign resCount=0>
<#assign totalResults=0>
<#if results?size = 0>
<div class="noItems">
   <span>(No results found)</span>
</div>
<#else>
   <#assign totalResults = results?size>
   <#list results as child>
      <#assign resCount=resCount + 1>
      <#if child.isDocument>
         <#assign relativePath = (child.displayPath?substring(companyhome.name?length+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
         <#if child.name?ends_with(extn) || child.name?ends_with(extnx)>
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
         <#if child.isLocked >
         <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="Locked" />
         <#elseif hasAspect(child, "cm:workingcopy") == 1>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In" /></a>
         <#else>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${child.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="Check Out" title="Check Out" /></a>
         </#if>
         <a href="${url.serviceContext}/office/myTasks?p=${path?url}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="Create Workflow..." title="Create Workflow..." /></a>
         <a href="#" onclick="window.external.insertDocument('${relativePath}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="Insert File into Current Document" title="Insert File into Current Document" /></a>
         <#if !child.name?ends_with(".pdf")>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF" /></a>
         </#if>
         <#if !child.isLocked>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','delete','${child.id}', 'Are you sure you want to delete this document?');"><img src="${url.context}/images/office/delete.gif" style="padding:3px 6px 2px 0px;" alt="Delete..." title="Delete" /></a>
         </#if>
      </#if>
   </span>
</div>
      <#if resCount = maxresults>
         <#break>
      </#if>
   </#list>
</#if>

<script type="text/javascript">
   OfficeSearch.itemsFound(${resCount}, ${totalResults});
</script>
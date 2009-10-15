<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign nav=args.n!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#if args.e??><#assign extList=[]><#else><#assign extList=[".odt", ".sxw", ".doc", ".rtf", ".ods", ".sxc", ".xls", ".odp", ".sxi", ".ppt", ".odg", ".sxd", ".odb", ".odf", ".sxm"]></#if>
<#if args.search??><#assign searchString = args.search><#else><#assign searchString=""></#if>
<#assign chLen=companyhome.name?length>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>

<#if args.maxresults??>
   <#assign maxresults=args.maxresults?number>
<#else>
   <#assign maxresults=10>
</#if>
<#assign resCount=0>
<#assign totalResults=0>
<#if results?size = 0>
<div class="noItems">
   <span>(${message("office.message.no_results")})</span>
</div>
<#else>
   <#assign totalResults = results?size>
   <#list results?sort_by(["properties","cm:modified"])?reverse as child>
      <#assign resCount=resCount + 1>
      <#assign isSupportedExtn = false>
      <#list extList as ext>
         <#if child.name?ends_with(ext)>
            <#assign isSupportedExtn = true>
            <#break>
         </#if>
      </#list>
      <#if child.isDocument>
         <#assign relativePath = child.displayPath?substring(chLen + 1) + '/' + child.name />
         <#if child.name?ends_with(extn) || child.name?ends_with(extnx) || isSupportedExtn>
            <#assign openURL = "#">
            <#assign hrefExtra = " onClick=\"ExternalComponent.openDocument('${relativePath?js_string}')\"">
         <#else>
            <#assign openURL = "${url.context}${child.url}">
            <#assign hrefExtra = " target=\"_blank\"">
         </#if>
      <#else>
         <#assign openURL = "${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${child.id}&amp;search=${searchString?url}&amp;maxresults=${maxresults}">
         <#assign hrefExtra = "">
      </#if>
<div class="documentItem ${(resCount % 2 = 0)?string("odd", "even")}">
   <span class="documentItemIcon">
      <a class="toolTip" href="${openURL}" ${hrefExtra} title="${child.displayPath?html}"><img src="${url.context}${child.icon32}" alt="${message("office.action.open", child.name?html)}" /></a>
   </span>
   <span class="documentItemDetails">
      <a class="bold toolTip" href="${openURL}" ${hrefExtra} title="${child.displayPath?html}">${child.name?html}</a><br />
      <#if child.properties.description??>
         <#if (child.properties.description?length > 0)>
            ${child.properties.description?html}<br />
         </#if>
      </#if>
      <#if child.isDocument>
         ${message("office.property.modified")}: ${child.properties.modified?datetime} (${(child.size / 1024)?int}${message("office.unit.kb")})<br />
         <#if child.isLocked >
         <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.status.locked")}" />
         <#elseif child.hasAspect("cm:workingcopy")>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${child.id}', '');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkin")}" title="${message("office.action.checkin")}" /></a>
         <#else>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${child.id}', '');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkout")}" title="${message("office.action.checkout")}" /></a>
         </#if>
         <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new&amp;wd=${child.id}"><img src="${url.context}/images/office/new_workflow.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.start_workflow")}..." title="${message("office.action.start_workflow")}..." /></a>
         <a href="#" onclick="ExternalComponent.insertDocument('${relativePath?js_string}', '${child.nodeRef}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.insert")}" title="${message("office.action.insert")}" /></a>
         <#if !child.name?ends_with(".pdf")>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${child.id}', '');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.transform_pdf")}" title="${message("office.action.transform_pdf")}" /></a>
         </#if>
         <#if !child.isLocked>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','delete','${child.id}', '${message("office.message.confirm_delete")}');"><img src="${url.context}/images/office/delete.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.delete")}..." title="${message("office.action.delete")}..." /></a>
         </#if>
      </#if>
   </span>
</div>
   </#list>
</#if>

<script type="text/javascript">
   OfficeSearch.itemsFound(${resCount}, ${totalResults});
</script>
<#-- Records Report - Template to apply to a records space to report on status of records -->

<style>
body {font:small/1.2em arial,helvetica,clean,sans-serif;font:x-small;margin-top: 10px; margin-right: 10px; margin-bottom: 0px; margin-left: 10px;min-width:500px;}
</style>

<#assign datetimeformat="dd MMM yyyy HH:mm">
<#assign xqueryformat="'yyyy-MM-dd'T'HH:mm:ss.000'Z'">
<#-- space.childrenByXPath[".//*[@rma:nextReviewDate < '${date?string(xqueryformat)}']"]?sort_by(['properties', 'rma:nextReviewDate']) -->

<#macro standardHeaders title extra="">
    <tr><td colspan="10"><h3>${title}</h3></td></tr>
    <tr Style="font-size:130%;font-weight:bold;color:#0000FF;">
        <td width=16></td>
        <td>ID</td>
        <td width=16></td>
        <td>Title</td>
        <td width=16></td>
        <td width=16></td>
        <td>File Plan</td>
        <td>Originator</td>
        <td>Date Filed</td>
        <td>${extra}</td>
    </tr>
</#macro>

<#macro standardProperties child extraProperty="">
    <tr>
    <!-- Set up workspace path to child and it's associated parent and file plan -->
    <#assign childRef=child.nodeRef>
    <#assign childWorkspace=childRef[0..childRef?index_of("://")-1]>
    <#assign childStorenode=childRef[childRef?index_of("://")+3..]>
    <#assign childPath="${childWorkspace}/${childStorenode}">

    <#if child.parent.hasAspect("rma:filePlan")>
        <#assign fileplan=child.parent>
    <#elseif child.parent.parent?exists && child.parent.hasAspect("rma:filePlan")>
        <#assign fileplan=child.parent.parent>
    <#else>
        <#assign fileplan=child.parent>
    </#if>

    <#assign fpRef=fileplan.nodeRef>
    <#assign fpWorkspace=fpRef[0..fpRef?index_of("://")-1]>
    <#assign fpStorenode=fpRef[fpRef?index_of("://")+3..]>
    <#assign fileplanPath="${fpWorkspace}/${fpStorenode}">

    <td width=16> <#-- Record properties icon -->
        <a href="${url.context}/navigate/showDocDetails/${childPath}">
        <img src="${url.context}/images/icons/View_details.gif" border=0 align=absmiddle alt="Record Details" title="Record Details"></a>
    </td>
    <td> <#-- Record identifier -->
        <a href="${url.context}/navigate/showDocDetails/${childPath}">
        ${child.properties["rma:recordIdentifier"]}</a>
    </td>
    <td width=16> <#-- Record icon -->
        <a href="${url.context}/download/direct/${childPath}/${child.name}">
        <img src="${url.context}${child.icon16}" width=16 height=16 border=0 align=absmiddle alt="View Record" title="View Record"></a>
    </td>
    <td> <#-- Record title -->
        <a href="${url.context}/download/direct/${childPath}/${child.name}">
        ${child.properties["cm:title"]}</a>
    </td>
    <td width=16> <#-- Fileplan icon -->
        <a href="${url.context}/navigate/browse/${fileplanPath}">
        <img src="${url.context}${fileplan.icon16}" width=16 height=16 border=0 align=absmiddle alt="Fileplan Contents" title="Fileplan Contents"></a>
    </td>
    <td width=16> <#-- Fileplan properties icon -->
        <a href="${url.context}/navigate/showDocDetails/${fileplanPath}">
        <img src="${url.context}/images/icons/View_details.gif" border=0 align=absmiddle alt="Fileplan Details" title="Fileplan Details"></a>
    </td>
    <td>
        <a href="${url.context}/navigate/showDocDetails/${fileplanPath}">
        ${fileplan.name}</a>
    </td>
    <td>
        ${child.properties["rma:originator"]}
    </td>
    <td>
        ${child.properties["rma:dateFiled"]?string(datetimeformat)}
    </td>
    <td>
        ${extraProperty}
    </td>
    </tr>
</#macro>

<#macro standardFooters>
    <tr><td colspan="10"><hr/></td></tr>
    <tr><td colspan="10"></td></tr>
</#macro>


<table width="100%"  border="0" cellpadding="1" cellspacing="1">

<@standardHeaders title="Recent Records" extra="Date"/>
<#list space.childrenByXPath[".//*[@rma:dateReceived]"]?sort_by(['properties', 'rma:dateReceived']) as child>
    <#if (dateCompare(child.properties["cm:modified"], date, 1000*60*60*24*7) == 1) || (dateCompare(child.properties["cm:created"], date, 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:dateReceived"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>


<@standardHeaders title="Vital Records Due for Review" extra="Next Review Due"/>
<#list space.childrenByXPath[".//*[@rma:nextReviewDate]"]?sort_by(['properties', 'rma:nextReviewDate']) as child>
    <#if (dateCompare(date, child.properties["rma:nextReviewDate"], 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:nextReviewDate"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>


<@standardHeaders title="Records Due for Cutoff" extra="Cutoff Date"/>
<#list space.childrenByXPath[".//*[@rma:cutoffDateTime]"]?sort_by(['properties', 'rma:cutoffDateTime']) as child>
    <#if (dateCompare(date, child.properties["rma:cutoffDateTime"], 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:cutoffDateTime"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>


<@standardHeaders title="Records Retention Due for Expiry" extra="Expiry Date"/>
<#list space.childrenByXPath[".//*[@rma:holdUntil]"]?sort_by(['properties', 'rma:holdUntil']) as child>
    <#if (dateCompare(date, child.properties["rma:holdUntil"], 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:holdUntil"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>


<@standardHeaders title="Records Due for Transfer" extra="Transfer Date"/>
<#list space.childrenByXPath[".//*[@rma:transferDate]"]?sort_by(['properties', 'rma:transferDate']) as child>
    <#if (dateCompare(date, child.properties["rma:transferDate"], 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:transferDate"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>


<@standardHeaders title="Records Due for Destruction" extra="Destruction Due Date"/>
<#list space.childrenByXPath[".//*[@rma:destructionDate]"]?sort_by(['properties', 'rma:destructionDate']) as child>
    <#if (dateCompare(date, child.properties["rma:destructionDate"], 1000*60*60*24*7) == 1)>
        <@standardProperties child=child extraProperty=child.properties["rma:destructionDate"]?string(datetimeformat) />
    </#if>
</#list>
<@standardFooters/>

</table>
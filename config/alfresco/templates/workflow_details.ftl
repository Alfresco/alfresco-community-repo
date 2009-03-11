<#if document?exists>
   <#if hasAspect(document, "app:simpleworkflow") = 1>
      This document has the following workflow:<br>
      <#if document.properties["app:approveStep"]?exists>
         <#assign ref=document.nodeRef>
         <#assign workspace=ref[0..ref?index_of("://")-1]>
         <#assign storenode=ref[ref?index_of("://")+3..]>
         &nbsp;&nbsp;Approve Step: <a href="${url.context}/command/workflow/approve/${workspace}/${storenode}">${document.properties["app:approveStep"]}</a><br>
      </#if>
      <#if document.properties["app:approveFolder"]?exists>
         &nbsp;&nbsp;Approve Folder: <a href="${url.context}${document.properties["app:approveFolder"].url}">${document.properties["app:approveFolder"].name}</a><br>
      </#if>
      <#if document.properties["app:rejectStep"]?exists>
         <#assign ref=document.nodeRef>
         <#assign workspace=ref[0..ref?index_of("://")-1]>
         <#assign storenode=ref[ref?index_of("://")+3..]>
         &nbsp;&nbsp;Reject Step: <a href="${url.context}/command/workflow/reject/${workspace}/${storenode}">${document.properties["app:rejectStep"]}</a><br>
      </#if>
      <#if document.properties["app:rejectFolder"]?exists>
         &nbsp;&nbsp;Reject Folder: <a href="${url.context}${document.properties["app:rejectFolder"].url}">${document.properties["app:rejectFolder"].name}</a><br>
      </#if>
   <#else>
      This document has no workflow.<br>
   </#if>
<#else>
   No document found!
</#if>
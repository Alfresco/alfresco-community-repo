<#assign count=0>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
   <#assign resources=task.packageResources>
   <#if resources?size != 0>
      <#list resources as res>
         <#assign count=count+1>
         <tr class="taskResource${(count % 2 = 0)?string("Odd", "Even")}">
            <td width="18"><a href="${url.context}${res.url}" target="new"><img src="${url.context}${res.icon16}" border=0></a></td>
            <td>
            <#if res.isDocument>
               <a class="resourceLink" onclick="event.cancelBubble=true;" href="${url.context}${res.url}" target="new">${res.name}</a>
            <#else>
               <span class="resourceLink">${res.name}</span>
            </#if>
            </td>
         </tr>
      </#list>
   <#else>
      <tr><td><span class="resourceLink">${msg("task_resource_panel.no_task_resources")}</span></td></tr>
   </#if>
</table>
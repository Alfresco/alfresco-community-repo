<table cellpadding='2' cellspacing='0'>
   <#list task.packageResources as res>
   <tr>
      <td><a href="${url.context}${res.url}" target="new"><img src="${url.context}${res.icon16}" border=0></a></td>
      <td>
         <#if res.isDocument>
            <a class="resourceLink" onclick="event.cancelBubble=true;" href="${url.context}${res.url}" target="new">${res.name}</a>
         <#else>
            <span class="resourceLink">${res.name}</span>
         </#if>
      </td>
   </tr>
   </#list>
</table>
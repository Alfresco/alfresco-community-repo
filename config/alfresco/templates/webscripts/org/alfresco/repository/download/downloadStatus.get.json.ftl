<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "status": "${downloadStatus.status?string}",
      "done": "${downloadStatus.done?string}", 
      "total": "${downloadStatus.total?string}", 
      "filesAdded": "${downloadStatus.filesAdded?string}", 
      "totalFiles": "${downloadStatus.totalFiles?string}" 
   }
</#escape>

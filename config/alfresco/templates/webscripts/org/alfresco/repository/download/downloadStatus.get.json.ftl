<#escape x as jsonUtils.encodeJSONString(x)>
   {
      "status": "${downloadStatus.status?string}",
      "done": "${downloadStatus.done?c}",
      "total": "${downloadStatus.total?c}",
      "filesAdded": "${downloadStatus.filesAdded?c}",
      "totalFiles": "${downloadStatus.totalFiles?c}"
   }
</#escape>

<#macro actionQItemStatusJSON actionQItemStatus>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "actionQueueItemStatusUrl" : "${url.serviceContext + "/api/actionqueue/items/"
            + actionQItemStatus.actionQueueItemId + "/status"}",
         "actionQueueItemId" : "{actionQItemStatus.actionQueueItemId}",
         "status" : "${actionQItemStatus.status}",
         "actionId" : "${actionQItemStatus.actionId}"
      }
   </#escape>
</#macro>

<#macro itemJSON item>
   <#escape x as jsonUtils.encodeJSONString(x)>
      <#assign node = item.node>
      <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
         "nodeRef": "${node.nodeRef}",
         "createdOn": "${xmldate(node.properties.created)}",
         "createdBy":
         {
            "value": "${item.createdBy.userName}",
            "displayValue": "${item.createdBy.displayName}"
         },
         "modifiedOn": "${xmldate(node.properties.modified)}",
         "modifiedBy":
         {
            "value": "${item.modifiedBy.userName}",
            "displayValue": "${item.modifiedBy.displayName}"
         },
         "actionSet": "${item.actionSet}",
         "tags": <#noescape>[${tags}]</#noescape>,
         "permissions":
         {
            "userAccess":
            {
         <#list item.actionPermissions?keys as actionPerm>
            <#if item.actionPermissions[actionPerm]?is_boolean>
               "${actionPerm?string}": ${item.actionPermissions[actionPerm]?string}<#if actionPerm_has_next>,</#if>
            </#if>
         </#list>
            }
         },
         <#if item.custom??>"custom": <#noescape>${item.custom}</#noescape>,</#if>
         "actionLabels":
         {
      <#if item.actionLabels??>
         <#list item.actionLabels?keys as actionLabel>
            "${actionLabel?string}": "${item.actionLabels[actionLabel]}"<#if actionLabel_has_next>,</#if>
         </#list>
      </#if>
         },
         "itemData":
         {
      <#list item.nodeData?keys as key>
         <#assign itemData = item.nodeData[key]>
            "${key}":
         <#if itemData?is_sequence>
            [
            <#list itemData as data>
               <@renderData data /><#if data_has_next>,</#if>
            </#list>
            ]
         <#else>
            <@renderData itemData />
         </#if><#if key_has_next>,</#if>
      </#list>
         }
   </#escape>
</#macro>

<#macro renderData data>
   <#escape x as jsonUtils.encodeJSONString(x)>
{
      <#if data.value?is_boolean>
   "value": ${data.value?string},
      <#elseif data.value?is_number>
   "value": ${data.value?c},
      <#else>
   "value": "${data.value}",
      </#if>
      <#if data.metadata??>
   "metadata": "${data.metadata}",
      </#if>
      <#if data.displayValue?is_boolean>
   "displayValue": ${data.displayValue?string}
      <#elseif data.displayValue?is_number>
   "displayValue": ${data.displayValue?c}
      <#else>
   "displayValue": "${data.displayValue}"
      </#if>
}
   </#escape>
</#macro>
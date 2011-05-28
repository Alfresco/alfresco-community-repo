<#macro transactionJSON txn>
      {
         "id": ${txn.id?c},
         "commitTimeMs": ${txn.commitTimeMs?c},
         "updates": ${txn.updates?c},
         "deletes": ${txn.deletes?c}
      }
</#macro>

<#macro nodeJSON node>
      {
         "id": ${node.id?c},
         "txnId": ${node.transaction.id?c},
         "status": "<#if node.deleted>d<#else>u</#if>"
      }
</#macro>

<#--
TODO property values: string, number, date (at the moment all output as string)
-->
<#macro nodeMetaDataJSON nodeMetaData filter>
      {
         "id": ${nodeMetaData.nodeId?c},
         "nodeRef": <#if filter.includeNodeRef??>"${nodeMetaData.nodeRef.toString()}",</#if>
         "type": <#if filter.includeType??><@qNameJSON qName=nodeMetaData.nodeType/>,</#if>
         "aclId": <#if filter.includeAclId??><#if nodeMetaData.aclId??>${nodeMetaData.aclId?c}<#else>null</#if>,</#if>
         <#if filter.includeProperties??>
         "properties": {
           <#list nodeMetaData.properties?keys as propQName>
               <@qNameJSON qName=propQName/>: ${nodeMetaData.properties[propQName]}<#if propQName_has_next>,</#if>
           </#list>
         },
         </#if>
         <#if filter.includeAspects??>
         "aspects": [
           <#list nodeMetaData.aspects as aspectQName>
               <@nodeAspectJSON aspectQName=aspectQName indent=""/><#if aspectQName_has_next>,</#if>
           </#list>
         ],
         </#if>
         <#if filter.includePaths??>
         "paths": [
           <#list nodeMetaData.paths as path>
               <@pathJSON path=path indent=""/><#if path_has_next>,</#if>
           </#list>
         ]
         </#if>
      }
</#macro>

<#macro pathJSON path indent="">
${indent}"${path.toString()}"
</#macro>

<#macro qNameJSON qName indent="">
${indent}"${jsonUtils.encodeJSONString(shortQName(qName))}"
</#macro>

<#macro nodePropertyJSON propQName propValue>
<@qNameJSON qName=propQName/>: <#if propValue??>"propValue"<#else>null</#if>
</#macro>

<#macro nodeAspectJSON aspectQName indent="">
${indent}<@qNameJSON qName=aspectQName/>
</#macro>



<#macro json_string string>${string?js_string?replace("\\'", "\'")?replace("\\>", ">")}</#macro>

<#macro aclChangeSetJSON aclChangeSet>
      {
         "id": ${aclChangeSet.id?c},
         "commitTimeMs": ${aclChangeSet.commitTimeMs?c},
         "aclCount": ${aclChangeSet.aclCount?c}
      }
</#macro>

<#macro aclJSON acl>
      {
         "id": ${acl.id?c},
         "aclChangeSetId": ${acl.aclChangeSetId?c}
      }
</#macro>

<#macro aclReadersJSON aclReaders>
      {
         "aclId": ${aclReaders.aclId?c},
         "aclChangeSetId": ${aclReaders.aclChangeSetId?c},
         "tenantDomain" : "${aclReaders.tenantDomain}",
         "readers" :
         [
            <#list aclReaders.readers as reader>
               "${reader?string}"
               <#if reader_has_next>,</#if>
            </#list>
         ]
      }
</#macro>

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
         "nodeRef": "${node.nodeRef}",
         "txnId": ${node.txnId?c},
         "status": "<#if node.deleted>d<#else>u</#if>"
      }
</#macro>

<#macro nodeMetaDataJSON nodeMetaData filter>
      {
         "id": ${nodeMetaData.nodeId?c}
         <#if nodeMetaData.tenantDomain??>, "tenantDomain": "${nodeMetaData.tenantDomain}"</#if>
         <#if filter.includeNodeRef??><#if nodeMetaData.nodeRef??>, "nodeRef": "${nodeMetaData.nodeRef.toString()}"</#if></#if>
         <#if filter.includeType??><#if nodeMetaData.nodeType??>, "type": <@qNameJSON qName=nodeMetaData.nodeType/></#if></#if>
         <#if filter.includeAclId??><#if nodeMetaData.aclId??>, "aclId": ${nodeMetaData.aclId?c}</#if></#if>
         <#if filter.includeTxnId??><#if nodeMetaData.txnId??>, "txnId": ${nodeMetaData.txnId?c}</#if></#if>
         <#if filter.includeProperties??>
         <#if nodeMetaData.properties??>
         , "properties": {
           <#list nodeMetaData.properties?keys as propName>
               "${propName}": ${nodeMetaData.properties[propName]}<#if propName_has_next>,</#if>
           </#list>
         }
         </#if>
         </#if>
         <#if filter.includeAspects??>
         <#if nodeMetaData.aspects??>
         , "aspects": [
           <#list nodeMetaData.aspects as aspectQName>
               <@nodeAspectJSON aspectQName=aspectQName indent=""/><#if aspectQName_has_next>,</#if>
           </#list>
         ]
         </#if>
         </#if>
         <#if filter.includePaths??>
         <#if nodeMetaData.paths??>
         , "paths": [
           <#list nodeMetaData.paths as path>
           ${path}<#if path_has_next>,</#if>
           </#list>
         ]
         </#if>
         </#if>
         <#if filter.includePaths??>
         <#if nodeMetaData.ancestors??>
         <#if (nodeMetaData.ancestors?size > 0)>
         , "ancestors": [
           <#list nodeMetaData.ancestors as ancestor>
           "${ancestor}"<#if ancestor_has_next>,</#if>
           </#list>
         ]
         </#if>
         </#if>
         </#if>
         <#if filter.includeParentAssociations??>
         <#if nodeMetaData.parentAssocs??>
         <#if (nodeMetaData.parentAssocs?size > 0)>
         , "parentAssocs": [
           <#list nodeMetaData.parentAssocs as pa>
           "<@json_string "${pa}"/>"<#if pa_has_next>,</#if>
           </#list>
         ]
         ,"parentAssocsCrc": <#if nodeMetaData.parentAssocsCrc??>${nodeMetaData.parentAssocsCrc?c}<#else>null</#if>
         </#if>
         </#if>
         </#if>
         <#if filter.includeChildAssociations??>
         <#if nodeMetaData.childAssocs??>
         <#if (nodeMetaData.childAssocs?size > 0)>
         , "childAssocs": [
           <#list nodeMetaData.childAssocs as ca>
           "<@json_string "${ca}"/>"<#if ca_has_next>,</#if>
           </#list>
         ]
         </#if>
         </#if>
         </#if>
         <#if filter.includeChildIds??>
         <#if nodeMetaData.childIds??>
         <#if (nodeMetaData.childIds?size > 0)>
         , "childIds": [
           <#list nodeMetaData.childIds as ci>
           ${ci?c}<#if ci_has_next>,</#if>
           </#list>
         ]
         </#if>
         </#if>
         </#if>
         <#if filter.includeOwner??>
         <#if nodeMetaData.owner??>
         , "owner": "${nodeMetaData.owner}"
         </#if>
         </#if>
      }
</#macro>

<#macro pathJSON path indent="">
${indent}[
<#list path as element>
${indent}${element}<#if element_has_next>,</#if>
</#list>
${indent}]
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



<#import "action.lib.ftl" as actionLib/>

<#macro ruleJSON rule>
   <#escape x as jsonUtils.encodeJSONString(x)>
      {
         "url" : "${url.serviceContext + "/api/rules/" + rule.nodeRef.storeRef.protocol + "/"
            + rule.nodeRef.storeRef.identifier + "/" + rule.nodeRef.id}",
         "ruleNodeRef" : "${rule.nodeRef}",
         "title" : "${rule.title}",
         "description" : "${rule.description}",
         "ruleTypes" :
         [
         <#list rule.ruleTypes as ruleType>
            "${ruleType}"
            <#if ruleType_has_next>,</#if>
         </#list>
         ],            
         "action" : <@actionLib.actionJSON action=rule.action/>,
         "actionableNodeRef" : "${actionableNodeRef}",
         "executeAsynchronously" : ${rule.executeAsynchronously?string("true", "false")},
         "ruleDisabled" : ${rule.ruleDisabled?string("true", "false")},
         "appliedToChildren" : ${rule.appliedToChildren?string("true", "false")}
      }
   </#escape>
</#macro>
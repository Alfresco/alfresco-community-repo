<#import "rule.lib.ftl" as ruleLib/>

[
   <#list rules as rule>
      <@ruleLib.ruleJSON rule=rule/>
      <#if rule_has_next>,</#if>
   </#list>
]
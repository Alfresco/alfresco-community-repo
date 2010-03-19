<#import "list.lib.ftl" as listLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "container": "${datalists.container.nodeRef?string}",
   "datalists":
   [
   <#list datalists.lists as list>
      <@listLib.listJSON list /><#if list_has_next>,</#if>
   </#list>
   ]
}
</#escape>
<#import "rmconstraint.lib.ftl" as rmconstraintLib/>

<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if !errorMessage??>
      "data": <@rmconstraintLib.constraintJSON constraint=constraint />
   <#else>
      "message" : "${msg(errorMessage, title)}"
   </#if>
}
</#escape>
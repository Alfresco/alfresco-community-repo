<#escape x as jsonUtils.encodeJSONString(x)>
{
   "suggestions" :
   [
       <#list suggestions as suggestion>
       {
            "weight" : ${suggestion.weight?c},
            "term" : "${suggestion.term}"
       }<#if suggestion_has_next>,</#if>
       </#list>
   ]
}
</#escape>
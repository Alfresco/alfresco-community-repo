<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "relationshipLabels":
      [
         <#list relationshipLabels as relationshipLabel>
         {
            "label": "${relationshipLabel.label}",
            "uniqueName": "${relationshipLabel.uniqueName}"
         }<#if relationshipLabel_has_next>,</#if>
         </#list>
      ]
   }
}
</#escape>
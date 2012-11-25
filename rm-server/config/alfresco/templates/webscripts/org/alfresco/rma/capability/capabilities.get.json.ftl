{
   "data":
   {
      <#if groupedCapabilities??>
      "groupedCapabilities":
      [
         <#assign keys = groupedCapabilities?keys>
         <#list keys as key>
         {
            "${key}":
            <#assign capabilitiesMap = groupedCapabilities[key]>
            <#assign capabilitiesKeys = capabilitiesMap?keys>
            {
            <#list capabilitiesKeys as capabilitiesKey>
               "${capabilitiesKey}": "${capabilitiesMap[capabilitiesKey]}"
               <#if capabilitiesKey_has_next>,</#if>
            </#list>
            }
         }<#if key_has_next>,</#if>
         </#list>
      ]
      <#else>
      "capabilities":
      [
         <#list capabilities as capability>
            "${capability}"<#if capability_has_next>,</#if>
         </#list>
      ]
      </#if>
   }
}
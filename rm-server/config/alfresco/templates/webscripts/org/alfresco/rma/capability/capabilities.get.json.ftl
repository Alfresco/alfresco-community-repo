{
   "data":
   {
      <#if groupedCapabilities??>
      "groupedCapabilities":
      [
         <#list groupedCapabilities?keys?sort as groupedCapabilityKey>
         {
            "${groupedCapabilityKey}":
            {
               <#assign groupedCapability = groupedCapabilities[groupedCapabilityKey]>
               groupTitle: "${groupedCapability.groupTitle}",
               capabilities:
               {
               <#assign capabilities = groupedCapability.capabilities>
               <#list capabilities?keys?sort as capabilityKey>
                  "${capabilityKey}": "${capabilities[capabilityKey]}"<#if capabilityKey_has_next>,</#if>
               </#list>
               }
            }
         }<#if groupedCapabilityKey_has_next>,</#if>
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
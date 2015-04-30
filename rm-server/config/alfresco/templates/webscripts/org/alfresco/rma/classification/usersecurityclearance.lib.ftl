<#macro usersecurityclearanceJSON item>
<#local cl=item.classificationLevel>
<#local pi=item.personInfo>
   <#escape x as jsonUtils.encodeJSONString(x)>
      "classificationId": "${cl.id}",
      "classificationLabel": "${cl.displayLabel}",
      "userName": "${pi.userName}",
      "firstName": <#if pi.firstName??>"${pi.firstName}"<#else>null</#if>,
      "lastName": <#if pi.lastName??>"${pi.lastName}"<#else>null</#if>
   </#escape>
</#macro>
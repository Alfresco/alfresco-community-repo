<#import "/org/alfresco/repository/generic-paged-results.lib.ftl" as gen/>

<#macro usersecurityclearanceJSON item>
   <#local cl=item.classificationLevel>
   <#local pi=item.personInfo>
   <#escape x as jsonUtils.encodeJSONString(x)>
      "classificationId": "${cl.id}",
      "classificationLabel": "${cl.displayLabel}",
      "userName": <#if pi.userName??>"${pi.userName}"<#else>null</#if>,
      "firstName": <#if pi.firstName??>"${pi.firstName}"<#else>null</#if>,
      "lastName": <#if pi.lastName??>"${pi.lastName}"<#else>null</#if>,
      "fullName": <#if pi.firstName?? && pi.lastName??>"${pi.firstName} ${pi.lastName}"<#else>"${pi.userName}"</#if>,
      "completeName": <#if pi.firstName?? && pi.lastName?? && pi.userName??>"${pi.firstName} ${pi.lastName} (${pi.userName})"<#else>"${pi.userName}"</#if>
   </#escape>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
   <@gen.pagedResults data=data ; item>
      {
      <@usersecurityclearanceJSON item=item />
      }
   </@gen.pagedResults>
   }
}
</#escape>
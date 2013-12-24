<#import "fileplanreport.lib.ftl" as reportLib/>
<#macro dateFormat date>${date?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   data:
   {
      "firstName": <#if person.properties.firstName??>"${person.properties.firstName}"<#else>null</#if>,
      "lastName": <#if person.properties.lastName??>"${person.properties.lastName}"<#else>null</#if>,
      <#if (recordSeries??)>
      "recordSeries": <@reportLib.recordSeriesJSON recordSeries=recordSeries/>,
      <#elseif (recordCategories??)>
      "recordCategories": <@reportLib.recordCategoriesJSON recordCategories=recordCategories/>,
      <#elseif (recordFolders??)>
      "recordFolders": <@reportLib.recordFoldersJSON recordFolders=recordFolders/>,
      </#if>
      "printDate": "<@dateFormat date=date/>"
   }
}
</#escape>
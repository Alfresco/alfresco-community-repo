<#macro recordSeriesJSON recordSeries>
<#escape x as jsonUtils.encodeJSONString(x)>
   [<#list recordSeries as recordSerie>
      {
         "parentPath": "${recordSerie.parentPath}",
         "name": "${recordSerie.name}",
         "identifier": "${recordSerie.identifier}",
         "description": "${recordSerie.description}",
         "recordCategories": <@recordCategoriesJSON recordCategories=recordSerie.recordCategories/>
      }<#if (recordSerie_has_next)>,</#if>
   </#list>]
</#escape>
</#macro>

<#macro recordCategoriesJSON recordCategories>
<#escape x as jsonUtils.encodeJSONString(x)>
   [<#list recordCategories as recordCategory>
      {
         "parentPath": "${recordCategory.parentPath}",                                             
         "name": "${recordCategory.name}",
         "identifier": "${recordCategory.identifier}",
         <#if (recordCategory.vitalRecordIndicator??)>"vitalRecordIndicator": ${recordCategory.vitalRecordIndicator?string},</#if>
         <#if (recordCategory.dispositionAuthority??)>"dispositionAuthority": "${recordCategory.dispositionAuthority}",</#if>
         "recordFolders":  <@recordFoldersJSON recordFolders=recordCategory.recordFolders/>,
         "dispositionActions": [<#list recordCategory.dispositionActions as dispositionAction>
            {
               "dispositionDescription": "${dispositionAction.dispositionDescription!""}"
            }<#if (dispositionAction_has_next)>,</#if>
         </#list>]
      }<#if (recordCategory_has_next)>,</#if>
   </#list>]
</#escape>
</#macro>

<#macro recordFoldersJSON recordFolders>
<#escape x as jsonUtils.encodeJSONString(x)>
   [<#list recordFolders as recordFolder>
      {
         "parentPath": "${recordFolder.parentPath}",
         "name": "${recordFolder.name}",         
         "identifier": "${recordFolder.identifier}",
         <#if (recordFolder.vitalRecordIndicator??)>"vitalRecordIndicator": "${recordFolder.vitalRecordIndicator?string}"</#if>
      }<#if (recordFolder_has_next)>,</#if>
   </#list>]
</#escape>
</#macro>
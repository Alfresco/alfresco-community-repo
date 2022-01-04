<#--
 #%L
 Alfresco Records Management Module
 %%
 Copyright (C) 2005 - 2022 Alfresco Software Limited
 %%
 This file is part of the Alfresco software.
 -
 If the software was purchased under a paid Alfresco license, the terms of
 the paid license agreement will prevail.  Otherwise, the software is
 provided under the following open source license terms:
 -
 Alfresco is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 -
 Alfresco is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 -
 You should have received a copy of the GNU Lesser General Public License
 along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 #L%
-->
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

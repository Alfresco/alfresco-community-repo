<#--
 #%L
 Alfresco Records Management Module
 %%
 Copyright (C) 2005 - 2021 Alfresco Software Limited
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

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
<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if !errorMessage??>
   "items":
   [
      <#list items as item>
      {
         "nodeRef": "${item.nodeRef}",
         "type": "${item.type}",
         "name": "${item.name}",
         "title": "${item.title!''}",
         "description": "${item.description!''}",
         "modifiedOn": "${xmldate(item.modifiedOn)}",
         "modifiedByUser": "${item.modifiedByUser}",
         "modifiedBy": "${item.modifiedBy}",
         "createdOn": "${xmldate(item.createdOn)}",
         "createdByUser": "${item.createdByUser}",
         "createdBy": "${item.createdBy}",
         "author": "${item.author!''}",
         "size": ${item.size?c},
         <#if item.browseUrl??>"browseUrl": "${item.browseUrl}",</#if>
         "parentFolder": "${item.parentFolder!""}",
         "properties":
         {
         <#assign first=true>
         <#list item.properties?keys as k>
            <#if item.properties[k]??>
               <#if !first>,<#else><#assign first=false></#if>"${k}":
               <#assign prop = item.properties[k]>
               <#if prop?is_date>"${xmldate(prop)}"
               <#elseif prop?is_boolean>${prop?string("true", "false")}
               <#elseif prop?is_enumerable>[<#list prop as p>"${p}"<#if p_has_next>, </#if></#list>]
               <#elseif prop?is_number>${prop?c}
               <#elseif prop?is_string>"${prop}"
               <#elseif prop?is_hash_ex>[<#list prop?values as p>"${p}"<#if p_has_next>, </#if></#list>]
               <#else>"${prop}"
               </#if>
            </#if>
         </#list>
         }
      }<#if item_has_next>,</#if>
      </#list>
   ]
   <#else>
      "errorMessage": "${errorMessage}"
   </#if>
}
</#escape>

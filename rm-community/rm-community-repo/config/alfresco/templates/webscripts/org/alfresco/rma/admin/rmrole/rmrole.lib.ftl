<#--
 #%L
 This file is part of Alfresco.
 %%
 Copyright (C) 2005 - 2016 Alfresco Software Limited
 %%
 Alfresco is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Alfresco is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
  
 You should have received a copy of the GNU Lesser General Public License
 along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 #L%
-->
<#-- renders an rm role object -->
<#macro roleJSON role>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "name": "${role.name}",
   "displayLabel": "${role.displayLabel}",
   "capabilities":
   {
   <#list role.capabilities as capability>
      "${capability.name}": "${capability.title}" <#if capability_has_next>,</#if>
   </#list>
   }
   <#if role.showAuths>
   ,
   "assignedUsers" :
   [
   <#list role.assignedUsers as user>
      {
      	"name" : "${user.name}",
      	"displayLabel" : "${user.displayLabel}"
      }<#if user_has_next>,</#if>
   </#list>
   ],
   "assignedGroups" :
   [
   <#list role.assignedGroups as group>
      {
      	"name" : "${group.name}",
      	"displayLabel" : "${group.displayLabel}"
      }<#if group_has_next>,</#if>
   </#list>
   ]
   <#if role.groupShortName??>
   ,"groupShortName": "${role.groupShortName}"
   </#if>
   </#if>
}
</#escape>
</#macro>
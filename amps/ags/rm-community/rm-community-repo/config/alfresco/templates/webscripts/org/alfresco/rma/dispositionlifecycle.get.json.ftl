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
   "data":
   {
      <#if nextaction.notFound?? && nextaction.notFound>
         "notFound": ${nextaction.notFound?string},
         "message": "${nextaction.message}"
      <#else>
         "url": "${nextaction.url}",
         "name": "${nextaction.name}",
         "label": "${nextaction.label}",
         "eventsEligible": ${nextaction.eventsEligible?string},
         <#if nextaction.asOf??>"asOf": "${nextaction.asOf}",</#if>
         <#if nextaction.startedAt??>"startedAt": "${nextaction.startedAt}",</#if>
         <#if nextaction.startedBy??>"startedBy": "${nextaction.startedBy}",</#if>
         <#if nextaction.startedByFirstName??>"startedByFirstName": "${nextaction.startedByFirstName}",</#if>
         <#if nextaction.startedByLastName??>"startedByLastName": "${nextaction.startedByLastName}",</#if>
         <#if nextaction.completedAt??>"completedAt": "${nextaction.completedAt}",</#if>
         <#if nextaction.completedBy??>"completedBy": "${nextaction.completedBy}",</#if>
         <#if nextaction.completedByFirstName??>"completedByFirstName": "${nextaction.completedByFirstName}",</#if>
         <#if nextaction.completedByLastName??>"completedByLastName": "${nextaction.completedByLastName}",</#if>
         "events":
         [
         <#list nextaction.events as event>
            {
               "name": "${event.name}",
               "label": "${event.label}",
               "complete": ${event.complete?string},
               <#if event.completedAt??>"completedAt": "${event.completedAt}",</#if>
               <#if event.completedBy??>"completedBy": "${event.completedBy}",</#if>
               <#if event.completedByFirstName??>"completedByFirstName": "${event.completedByFirstName}",</#if>
               <#if event.completedByLastName??>"completedByLastName": "${event.completedByLastName}",</#if>
               "automatic": ${event.automatic?string}
            }<#if event_has_next>,</#if>
         </#list>
         ]
      </#if>
   }
}
</#escape>

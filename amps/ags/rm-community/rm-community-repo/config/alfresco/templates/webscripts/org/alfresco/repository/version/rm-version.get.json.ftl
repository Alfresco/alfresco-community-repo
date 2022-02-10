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
[
<#list versions as v>
   {
      "nodeRef": "${v.nodeRef}",
      "name": "${v.name}",
      "label": "${v.label}",
      "description": "${v.description}",
      "createdDate": "${v.createdDate?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
      "createdDateISO": "${xmldate(v.createdDate)}",
      "creator":
      {
         "userName": "${v.creator.userName}",
         "firstName": "${v.creator.firstName!""}",
         "lastName": "${v.creator.lastName!""}"
      },
      "recordNodeRef": "${v.recordNodeRef}",
      "isRecordedVersionDestroyed": ${v.isRecordedVersionDestroyed?c}
   }<#if (v_has_next)>,</#if>
</#list>
]
</#escape>

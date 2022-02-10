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
	    "users":
		{
			<#list report.users?keys as user>			
            "${user}":
            {
                "userName": "${report.users[user].userName!""}",
                "firstName": "${report.users[user].firstName!""}",
                "lastName": "${report.users[user].lastName!""}",
                "roles": [<#list report.users[user].roles as role>"${role}"<#if role_has_next>,</#if></#list>],
                "groups": [<#list report.users[user].groups as group>"${group}"<#if group_has_next>,</#if></#list>]
            }
            <#if user_has_next>,</#if>
            </#list>
		},
		"roles":
		{
			<#list report.roles?keys as role>         
            "${role}":
            {
                "name": "${report.roles[role].name!""}",
                "label": "${report.roles[role].displayLabel!""}",
                "users": [<#list report.roles[role].users as user>"${user}"<#if user_has_next>,</#if></#list>],
                "capabilities": [<#list report.roles[role].capabilities as capability>"${capability}"<#if capability_has_next>,</#if></#list>]
            }
            <#if role_has_next>,</#if>
            </#list>
		},
		"groups":
        {
            <#list report.groups?keys as group>         
            "${group}":
            {
                "name": "${report.groups[group].name!""}",
                "label": "${report.groups[group].displayLabel!""}",
                "users": [<#list report.groups[group].users as user>"${user}"<#if user_has_next>,</#if></#list>]
            }
            <#if group_has_next>,</#if>
            </#list>
        }
	}
}
</#escape>

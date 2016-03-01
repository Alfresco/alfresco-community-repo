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
{
    "message" : "${message}"
<#if result?exists>
    ,"result" : "${result?string}"
</#if>
<#if results?exists>
    ,"results" : 
    {    
    <#list results?keys as prop>
    	"${prop}" : "${results[prop]}"<#if prop_has_next>,</#if> 		
    </#list>
    }
</#if>
}
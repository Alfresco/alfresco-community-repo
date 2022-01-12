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
      "customProperties":
      {
         <#list customProps as prop>
         "${prop.name.toPrefixString()}":
         {
            "dataType": "<#if prop.dataType??>${prop.dataType.name.toPrefixString()}</#if>",
            "label": "${prop.title!""}",
            "description": "${prop.description!""}",
            "mandatory": ${prop.mandatory?string},
            "multiValued": ${prop.multiValued?string},
            "defaultValue": "${prop.defaultValue!""}",
            "protected": ${prop.protected?string},
            "propId": "${prop.name.localName}",
            "constraintRefs":
            [
               <#list prop.constraints as con>
               {
                  "name": "${con.constraint.shortName!""}",
                  "title": "${msg(con.constraint.title)!""}",
                  "type": "${con.constraint.type!""}",
                  "parameters":
                  {
                     <#-- Basic implementation. Only providing 2 hardcoded parameters. -->
                     <#assign lov = con.constraint.parameters["allowedValues"]>
                     "caseSensitive": ${con.constraint.parameters["caseSensitive"]?string},
                     "listOfValues" :
                     [
                        <#list lov as val>"${val}"<#if val_has_next>,</#if></#list>
                     ]
                  }
               }<#if con_has_next>,</#if>
               </#list>
            ]
         }<#if prop_has_next>,</#if>
         </#list>
      }
   }
}
</#escape>

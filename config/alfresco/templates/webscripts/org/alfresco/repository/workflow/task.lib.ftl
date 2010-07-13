<#-- Renders a task instance. -->
<#macro taskJSON task >
      {
         "id": "${task.id}",
         "url": "${task.url}",
         "name": "${task.name}",
         "title": "${task.title}",
         "description": "${task.description}",
         "state": "${task.state}",
         "typeDefinitionTitle": "${task.typeDefinitionTitle}",
         "isPooled": ${task.isPooled?string},
         "owner":
         <#if task.owner??>
         {
            "userName": "${task.owner.userName}",
            "firstName": "${task.owner.firstName}",
            "lastName": "${task.owner.lastName}"
         },
         <#else>
            null,
         </#if>
         "properties":
         <@propertiesJSON properties=task.properties />
      }
</#macro>

<#-- Renders a map of properties -->
<#macro propertiesJSON properties>
   {
   <#list properties?keys as key>
      "${key}" :
      <#if properties[key]??>
         <#assign val=properties[key]>
            <#if val?is_boolean == true>
               ${val?string}
            <#elseif val?is_number == true>
               ${val?c}
            <#elseif val?is_sequence>
               [
               <#list val as element>
                  "${jsonUtils.encodeJSONString(element?string)}"<#if (element_has_next)>,</#if>
               </#list>
               ]
            <#else>
               "${jsonUtils.encodeJSONString(shortQName(val?string))}"
            </#if>
      <#else>
         null
      </#if>
      <#if (key_has_next)>,</#if>
   </#list>
   }
</#macro>

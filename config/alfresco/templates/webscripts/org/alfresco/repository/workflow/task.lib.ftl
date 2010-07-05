<#-- Renders a task instance. -->
<#macro taskJSON task >
        {
            "url": "${task.url}",
            "name": "${task.name}",
            "title": "${task.title}",
            "description": "${task.description}",
            "state": "${task.state}",
            "typeDefinitionTitle": "${task.typeDefinitionTitle}",
            "isPooled": ${task.isPooled?string},
            <#if task.owner??>
                "owner":
                {
                    "userName": "${task.owner.userName}",
                    "firstName": "${task.owner.firstName}",
                    "lastName": "${task.owner.lastName}"
                },
            </#if>
            "properties":
                <@propertiesJSON properties=task.properties />
        }
</#macro>

<#-- Renders a map of properties -->
<#macro propertiesJSON properties>
    {
    <#list properties?keys as key>
        <#if properties[key]??>
            <#assign val=properties[key]>
                "${key}" :
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
            <#if (key_has_next)>,</#if>
        </#if>
    </#list>
    }    
</#macro>
   
  
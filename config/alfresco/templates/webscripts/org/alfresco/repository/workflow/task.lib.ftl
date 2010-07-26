<#-- Renders a task instance. -->
<#macro taskJSON task detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
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
         <#if detailed>,
         "path": "${task.path}",
         "definition":
         {
            "id": "${task.definition.id}",
            "url": "${task.definition.url}",
            "type": 
            {
               "name": "${shortQName(task.definition.type.name?string)}",
               "title": "${task.definition.type.title}",
               "description": "${task.definition.type.description}",
               "url": "${task.definition.type.url}"
            },
            "node": 
            {
               "name": "${task.definition.node.name}",
               "title": "${task.definition.node.title}",
               "description": "${task.definition.node.description}",
               "isTaskNode": ${task.definition.node.isTaskNode?string},
               "transitions": 
               [
                  <#list task.definition.node.transitions as transition>
                  {
                     "id": "${transition.id}",
                     "title": "${transition.title}",
                     "description": "${transition.description}",
                     "isDefault": ${transition.isDefault?string},
                     "isHidden": ${transition.isHidden?string}
                  }
                  <#if transition_has_next>,</#if>
                  </#list>
               ]
            }
         }
         </#if>
        }
</#escape>
</#macro>

<#-- Renders a map of properties -->
<#macro propertiesJSON properties>
<#escape x as jsonUtils.encodeJSONString(x)>
    {
    <#list properties?keys as key>
        "${key}":
        <#if properties[key]??>
            <#assign val=properties[key]>
            <#if val?is_boolean == true>
               ${val?string}
            <#elseif val?is_number == true>
               ${val?c}
            <#elseif val?is_sequence>
               [
               <#list val as element>
                  "${element?string}"<#if (element_has_next)>,</#if>
               </#list>
               ]
            <#else>
               "${shortQName(val?string)}"
            </#if>
      <#else>
         null
      </#if>
      <#if (key_has_next)>,</#if>
   </#list>
   }
</#escape>
</#macro>

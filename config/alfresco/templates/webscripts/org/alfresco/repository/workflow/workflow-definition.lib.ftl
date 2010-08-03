<#-- Renders a workflow definition. -->
<#macro workflowDefinitionJSON workflowDefinition detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id" : "${workflowDefinition.id}",
         "url": "${workflowDefinition.url}",
         "name": "${workflowDefinition.name}",
         "title": "${workflowDefinition.title}",
         "description": "${workflowDefinition.description}"
         <#if detailed>,
         "version": "${workflowDefinition.version}",
         "startTaskDefinitionUrl": "${workflowDefinition.startTaskDefinitionUrl}",
         "startTaskDefinitionType": "${shortQName(workflowDefinition.startTaskDefinitionType)}",
         "taskDefinitions": 
         [
            <#list workflowDefinition.taskDefinitions as taskDefinition>
            {
               "url": "${taskDefinition.url}",
               "type": "${shortQName(taskDefinition.type)}"
            }
            <#if taskDefinition_has_next>,</#if>
            </#list>
         ]
         </#if>
      }
</#escape>
</#macro>
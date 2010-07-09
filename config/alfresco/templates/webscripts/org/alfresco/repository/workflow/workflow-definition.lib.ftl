<#-- Renders a workflow definition. -->
<#macro workflowDefinitionJSON workflowDefinition>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id" : "${workflowDefinition.id}",
         "url": "${workflowDefinition.url}",
         "name": "${workflowDefinition.name}",
         "title": "${workflowDefinition.title}",
         "description": "${workflowDefinition.description}"
      }
</#escape>
</#macro>
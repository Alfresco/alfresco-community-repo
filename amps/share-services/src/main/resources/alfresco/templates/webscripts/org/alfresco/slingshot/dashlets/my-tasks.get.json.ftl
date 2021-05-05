<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>
<#assign inviteWorkflowDefinitionNames = ["jbpm$imwf:invitation-moderated", "jbpm$inwf:invitation-nominated"]>
<#assign filter = args["filter"]!"all">
<#--
   Resolve site, container and path
-->
<#macro location doc>
   <#assign qnamePaths = doc.qnamePath?split("/")>
   <#assign displayPaths = doc.displayPath?split("/") + [""]>
   <#if ((qnamePaths?size &gt; 5) && (qnamePaths[2] == "st:sites"))>
               "site": "${displayPaths[3]}",
               "container": "${displayPaths[4]}",
               "path": "<#list displayPaths[5..] as path><#if path_has_next>/</#if>${path}</#list>"
   </#if>
</#macro>
<#--
   Render a task
-->
<#macro dateFormat date>${date?datetime?string("yyyy-MM-dd HH:mm:ss 'GMT'Z '('zzz')'")!""}</#macro>
<#macro taskDetail task>
   {
      "id": "${task.id}",
      "description": "${(task.description!"")?j_string}",
      "dueDate": "<#if task.properties["bpm:dueDate"]?exists><@dateFormat task.properties["bpm:dueDate"] /><#else><@dateFormat future /></#if>",
      "status": "${task.properties["bpm:status"]}",
      "priority": "${task.properties["bpm:priority"]}",
      "startDate": "<@dateFormat task.startDate />",
      "type": "${task.type}",
      "completeness": "${task.properties["bpm:percentComplete"]}",
      "resources":
      [
<#list task.packageResources as resource>
         {
            "nodeRef": "${resource.nodeRef}",
            "fileName": "${resource.name}",
            "displayName": "${resource.name?replace(workingCopyLabel, "")}",
            "location":
            {
            <@location resource />
            },
            "icon": "${resource.icon16}"
         }<#if resource_has_next>,</#if>
</#list>
      ],
      "transitions":
      [
<#list task.transitions as transition>
         {
            "id": "${transition.id!""}",
            "label": "${transition.label!""}"
         }<#if transition_has_next>,</#if>
</#list>
      ]
   }
</#macro>

<#macro invitationModeratedTaskDetail task>
   <#assign theSite = site.getSiteInfo(task.properties["imwf:resourceName"])>
   <#assign theUser = people.getPerson(task.properties["imwf:inviteeUserName"])>
   {
      "id": "${task.id}",
      "description": "${(task.description!"")?j_string}",
      "dueDate": "<#if task.properties["bpm:dueDate"]?exists><@dateFormat task.properties["bpm:dueDate"] /><#else><@dateFormat future /></#if>",
      "status": "${task.properties["bpm:status"]}",
      "priority": "${task.properties["bpm:priority"]}",
      "startDate": "<@dateFormat task.startDate />",
      "type": "${task.type}",
      "completeness": "${task.properties["bpm:percentComplete"]}",
      "invitation":
      {
         "type": "moderated",
         "site":
         {
            "id": "${theSite.shortName}",
            "title": "${theSite.title!""}",
            "description": "${theSite.description!""}"
         },
         "invitee":
         {
            "fullName": "${(theUser.properties.firstName!"" + " " + theUser.properties.lastName!"")?trim}",
         <#if theUser.assocs["cm:avatar"]??>
            "avatarRef": "${theUser.assocs["cm:avatar"][0].nodeRef?string}",
         </#if>
            "userName": "${theUser.properties.userName}"
         },
         "inviteeRole": "${task.properties["imwf:inviteeRole"]}"
      },
      "transitions":
      [
<#list task.transitions as transition>
         {
            "id": "${transition.id!""}",
            "label": "${transition.label!""}"
         }<#if transition_has_next>,</#if>
</#list>
      ]
   }
</#macro>

<#macro invitationNominatedTaskDetail task>
   {
      "id": "${task.id}",
      "description": "${(task.description!"")?j_string}",
      "dueDate": "<#if task.properties["bpm:dueDate"]?exists><@dateFormat task.properties["bpm:dueDate"] /><#else><@dateFormat future /></#if>",
      "status": "${task.properties["bpm:status"]}",
      "priority": "${task.properties["bpm:priority"]}",
      "startDate": "<@dateFormat task.startDate />",
      "type": "${task.type}",
      "completeness": "${task.properties["bpm:percentComplete"]}",
      <#if task.properties["inwf:resourceName"]?exists>
      <#assign theInviter = people.getPerson(task.properties["inwf:inviterUserName"])>
      "invitation":
      {
         "type": "nominated",
         "site":
         {
            "id": "${task.properties["inwf:resourceName"]!""}",
            "title": "${jsonUtils.encodeJSONString(task.properties["inwf:resourceTitle"]!"")}",
            "description": "${jsonUtils.encodeJSONString(task.properties["inwf:resourceDescription"]!"")}"
         },
         "inviter":
         {
            "fullName": "${(theInviter.properties.firstName!"" + " " + theInviter.properties.lastName!"")?trim}",
         <#if theInviter.assocs["cm:avatar"]??>
            "avatarRef": "${theInviter.assocs["cm:avatar"][0].nodeRef?string}",
         </#if>
            "userName": "${theInviter.properties.userName}"
         },
         "inviteeRole": "${task.properties["inwf:inviteeRole"]}"
      },
      </#if>
      "transitions":
      [
<#list task.transitions as transition>
         {
            "id": "${transition.id!""}",
            "label": "${transition.label!""}"
         }<#if transition_has_next>,</#if>
</#list>
      ]
   }
</#macro>


<#--
   Filter task list
-->
<#assign filteredTasks = []>
<#assign unfilteredTasks = workflow.assignedTasks + workflow.pooledTasks>
<#list unfilteredTasks as task>
   <#assign hasDate = task.properties["bpm:dueDate"]?exists>
   <#assign dueDate><#if task.properties["bpm:dueDate"]?exists>${task.properties["bpm:dueDate"]?date!""}<#else>${future?date}</#if></#assign>
   <#switch filter>
      <#case "all">
         <#assign filteredTasks = filteredTasks + [task]>
         <#break>

      <#case "today">
         <#if (dateCompare(date?date, dueDate?date, 0, "==") == 1)>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "tomorrow">
         <#if (dateCompare(tomorrow?date, dueDate?date, 0, "==") == 1)>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "this-week">
         <#if ((dateCompare(lastSunday?date, dueDate?date) == 0) && (dateCompare(sunday?date, dueDate?date) == 1))>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "next-week">
         <#if ((dateCompare(sunday?date, dueDate?date) == 0) && (dateCompare(nextSunday?date, dueDate?date) == 1))>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "overdue">
         <#if (dateCompare(date?date, dueDate?date) == 1)>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "no-due-date">
         <#if !hasDate>
            <#assign filteredTasks = filteredTasks + [task]>
         </#if>
         <#break>

      <#case "invites">
         <#if task.properties["bpm:package"]??>
            <#if inviteWorkflowDefinitionNames?seq_contains(task.properties["bpm:package"].properties["bpm:workflowDefinitionName"])>
               <#assign filteredTasks = filteredTasks + [task]>
            </#if>
         </#if>
         <#break>
   </#switch>
</#list>

<#escape x as jsonUtils.encodeJSONString(x)>
{
   "tasks":
   [
<#list filteredTasks as task>
   <#if task.properties["bpm:package"]??>
      <#assign packageNode = task.properties["bpm:package"]>
      <#if packageNode.properties["bpm:workflowDefinitionName"] == "jbpm$imwf:invitation-moderated">
         <@invitationModeratedTaskDetail task />
      <#elseif packageNode.properties["bpm:workflowDefinitionName"] == "jbpm$inwf:invitation-nominated">
         <@invitationNominatedTaskDetail task />
      <#else>
         <@taskDetail task />
      </#if>
   </#if>
   <#if task_has_next>,</#if>
</#list>
   ]
}
</#escape>
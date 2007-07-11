<#assign weekms=1000*60*60*24*7>
<#assign count=0>
<#list workflow.assignedTasks as t>
   <#-- TODO: is it better to use a js script to pre-filter the list? -->
   <#assign hasDue=t.properties["bpm:dueDate"]?exists>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
   <#-- filters: 0=all, 1=today, 2=next week, 3=no due date, 4=overdue -->
   <#if (args.f="0") ||
        (args.f="3" && !hasDue) ||
        (args.f="1" && hasDue && (dateCompare(date?date, due?date, 0, "==") == 1)) ||
        (args.f="2" && hasDue && (dateCompare(due?date, date?date) == 1 && dateCompare(date?date, due?date, weekms) == 1)) ||
        (args.f="4" && hasDue && (dateCompare(date?date, due?date) == 1))>
   <#assign count=count+1>
   <div class="taskRow" id="${t.id}" rel="<#if hasDue>${due?date?string("yyyyMMddHHmmss")}<#else>99999999999999</#if>">
      <div class="taskTitle">
         <div class="taskIndicator">
         <#if hasDue>
            <#-- items due today? -->
            <#if (args.f="0" || args.f="1") && (dateCompare(date?date, due?date, 0, "==") == 1)>
               <img src="${url.context}/images/icons/task_today.gif"></div><div class="taskItem taskItemToday">
            <#-- items overdue? -->
            <#elseif (args.f="0" || args.f="4") && (dateCompare(date?date, due?date) == 1)>
               <img src="${url.context}/images/icons/task_overdue.gif"></div><div class="taskItem taskItemOverdue">
            <#else>
               </div><div class="taskItem">
            </#if>
         <#else>
            </div><div class="taskItem">
         </#if>
            ${t.description?html} (${t.type?html})
            <#if hasDue>
               (Due: ${due?date})
            </#if>
            <span class="taskInfo" onclick="event.cancelBubble=true; TaskInfoMgr.toggle('${t.id}',this);">
               <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
            </span>
         </div>
      </div>
      <div class="taskDetail">
         <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
               <td width="182">
                  <table cellpadding="2" cellspacing="2" style="margin-left:24px; margin-top:4px">
                     <tr><td class="taskMetaprop">Status:</td><td class="taskMetadata">${t.properties["bpm:status"]}</td>
                     <tr><td class="taskMetaprop">Priority:</td><td class="taskMetadata">${t.properties["bpm:priority"]}</td>
                     <tr><td class="taskMetaprop">Start Date:</td><td class="taskMetadata">${t.startDate?date}</td></tr>
      	            <tr><td class="taskMetaprop">Type:</td><td class="taskMetadata">${t.type?html}</td></tr>
                     <tr><td class="taskMetaprop">Complete:</td><td class="taskMetadata">${t.properties["bpm:percentComplete"]}%</td>
      	         </table>
      	      </td>
               <td width="8">&nbsp;</td>
      	      <td width="360">
                  <div class="taskResourceHeader">${t.name?html}:</div>
                  <div class="taskResources"></div>
      	         <table border="0" class="taskActions">
      	            <tr>
      	               <#list t.transitions as wt>
      	               <td style="text-align: left"><a class="taskAction" href="#" onclick="event.cancelBubble=true; MyTasks.transitionTask('/command/task/end/${t.id}<#if wt.id?exists>/${wt.id}</#if>', 'Workflow action \'${wt.label?html}\' completed.');">${wt.label?html}</a></td>
      	               </#list>
      	               <td width="70">&nbsp;</td>
      	            </tr>
      	         </table>
               </td>
               <td width="16">&nbsp;</td>
               <td>
                  <div class="taskMetaprop" style="padding-bottom: 4px;">Manage Task</div>
                  <a class="taskAction" style="display: block; width: 18px; padding: 4px; margin-left: 20px;" onclick="event.cancelBubble=true;" href="${url.context}/command/ui/managetask?id=${t.id}&type=${t.qnameType}&container=plain" target="new"><img src="${url.context}/images/icons/manage_workflow_task.gif" width="16" height="16" border="0" alt="Manage Task Details" title="Manage Task Details"></a>
                  <br>
                  <br>
               </td>
            </tr>
	      </table>
      </div>
   </div>
   </#if>
</#list>
<#-- hidden div with the count value for the page -->
<div id="taskCountValue" style="display:none">${count}</div>
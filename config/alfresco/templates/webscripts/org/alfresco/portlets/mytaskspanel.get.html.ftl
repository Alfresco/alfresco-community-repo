<#assign weekms=1000*60*60*24*7>
<#assign count=0>
<#list workflow.assignedTasks?sort_by('startDate') as t>
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
   <div class="taskRow" id="${t.id}">
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
         <div style="float:left">
            <table cellpadding='2' cellspacing='0' style="margin-left:32px;margin-top:16px">
               <tr><td class="taskMetaprop">Status:</td><td class="taskMetadata">${t.properties["bpm:status"]}</td>
               <tr><td class="taskMetaprop">Priority:</td><td class="taskMetadata">${t.properties["bpm:priority"]}</td>
               <tr><td class="taskMetaprop">Start Date:</td><td class="taskMetadata">${t.startDate?date}</td></tr>
	            <tr><td class="taskMetaprop">Type:</td><td class="taskMetadata">${t.type?html}</td></tr>
               <tr><td class="taskMetaprop">Complete:</td><td class="taskMetadata">${t.properties["bpm:percentComplete"]}%</td>
	         </table>
         </div>
         <div class="taskResourceHeader">${t.name?html}:</div>
         <div class="taskResources"></div>
         <div>
	         <table class="taskActions" style="padding-left:16px">
	            <tr>
	               <#list t.transitions as wt>
	               <td><a class="taskAction" href="#" onclick="event.cancelBubble=true; MyTasks.transitionTask('/command/task/end/${t.id}<#if wt.id?exists>/${wt.id}</#if>', 'Workflow action \'${wt.label?html}\' completed.');">${wt.label?html}</a></td>
	               </#list>
	            </tr>
	         </table>
	      </div>
      </div>
   </div>
   </#if>
</#list>
<#-- hidden div with the count value for the page -->
<div id="taskCountValue" style="display:none">${count}</div>
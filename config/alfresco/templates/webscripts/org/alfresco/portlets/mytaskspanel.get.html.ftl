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
         <#if t.description?exists>
            ${t.description?html} (${t.type?html})
         <#else>
            ${t.type?html} (${t.type?html})
         </#if>
            <#if hasDue>
               (Due: ${due?date})
            </#if>
            <span class="taskInfo" onclick="event.cancelBubble=true; TaskInfoMgr.toggle('${t.id}',this);">
               <img src="${url.context}/images/icons/popup.gif" class="popupImage" width="16" height="16" />
            </span>
         </div>
      </div>
      <div class="taskDetail">
         <div class="taskDetailTopSpacer"></div>
         <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
               <td width="190" class="taskDetailSeparator">
                  <table cellpadding="2" cellspacing="2" style="margin-left:24px; margin-top:4px">
                     <tr><td class="taskMetaprop">${message("portlets.mytaskspanel.status")}:</td><td class="taskMetadata">${t.properties["bpm:status"]}</td>
                     <tr><td class="taskMetaprop">${message("portlets.mytaskspanel.priority")}:</td><td class="taskMetadata">${t.properties["bpm:priority"]}</td>
                     <tr><td class="taskMetaprop">${message("portlets.mytaskspanel.start_date")}:</td><td class="taskMetadata">${t.startDate?date}</td></tr>
                     <tr><td class="taskMetaprop">${message("portlets.mytaskspanel.complete")}:</td><td class="taskMetadata">${t.properties["bpm:percentComplete"]}%</td>
      	         </table>
      	      </td>
               <td width="8">&nbsp;</td>
      	      <td width="300">
                  <div class="taskResourceHeader">${t.name?html}:</div>
                  <div class="taskResources"></div>
               </td>
               <td width="8" class="taskDetailSeparator">&nbsp;</td>
               <td align="center">
                  <table cellpadding="0" cellspacing="0" border="0">
                  <tr>
                     <td>
                        <div class="taskManage">
                           <ul>
                              <li><a href="#" onclick="event.cancelBubble=true; openWindowCallback('${url.context}/command/ui/managetask?id=${t.id}&amp;type=${t.qnameType}&amp;container=plain', MyTasks.manageTaskCallback);"><span style="white-space:nowrap"><img src="${url.context}/images/icons/manage_workflow_task.gif" align="top" alt="" border="0"> ${message("portlets.mytaskspanel.manage_task")}</span></a></li>
                           </ul>
                        </div>
                     </td>
                  </tr>
                  </table>
               </td>
            </tr>
            <tr>
               <td colspan="2">&nbsp;</td>
               <td width="300" align="center">
                  <table cellpadding="0" cellspacing="0" border="0">
                  <tr>
                     <td>
                        <div class="taskAction">
         	               <ul>
      	                  <#list t.transitions as wt>
         	                  <li><a href="#" onclick="event.cancelBubble=true; MyTasks.transitionTask('/command/task/end/${t.id}<#if wt.id?exists>/${wt.id}</#if>', '${message("portlets.mytaskspanel.workflow_action")} \'${wt.label?html}\' ${message("portlets.mytaskspanel.workflow_complited")}.');"><span>${wt.label?html}</span></a></li>
      	                  </#list>
         	               </ul>
         	            </div>
   	               </td>
   	            </tr>
   	            </table>
               </td>
               <td colspan="2">&nbsp;</td>
            </tr>
	      </table>
      </div>
   </div>
   </#if>
</#list>
<#-- hidden div with the error message -->
<div id="displayTheError" style="display:none">${message("portlets.error.data_currently_unavailable")}</div>
<#-- hidden div with the count value for the page -->
<div id="taskCountValue" style="display:none">${count}</div>
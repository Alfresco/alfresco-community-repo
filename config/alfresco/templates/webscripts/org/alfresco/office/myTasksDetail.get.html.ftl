<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if>
<#if args.t?exists>
   <#assign taskid = args.t>
   <#if taskid != "">
      <#assign task = workflow.getTaskById(taskid)>
   </#if>
</#if>

<#if task?exists>
   <table width="100%">
      <tr>
         <td width="32" valign="top"><img src="${url.context}/images/office/task_item.gif" alt="Task item" /></td>
         <td>
            <span style="font-weight: bold;">${task.description?html}</span>
            <br />     
            <table style="margin-top: 4px;">
               <tr>
                  <td>Status:</td>
                  <td>${task.properties["bpm:status"]}</td>
               </tr>
               <tr>
                  <td>Priority:</td>
                  <td>${task.properties["bpm:priority"]}</td>
               </tr>
               <tr>
                  <td>Start Date:</td>
                  <td>${task.startDate?date}</td>
               </tr>
               <tr>
                  <td>Type:</td>
                  <td>${task.type?html}</td>
               </tr>
               <tr>
                  <td>Complete:</td>
                  <td>${task.properties["bpm:percentComplete"]}%</td>
               </tr>
            </table>
         </td>
      </tr>
   </table>
   
   <div style="margin-top: 8px; font-weight: bold;">${task.name?html}:</div>
   <div class="taskResources">
      <table width="100%">
   <#list task.packageResources as res>
         <tr>
      <#if res.isDocument>
         <#if res.name?ends_with(extn)>
            <#assign relativePath = (res.displayPath?substring(companyhome.name?length+1) + '/' + res.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
            <td width="16"><a href="${url.context}${res.url}" target="new"><img src="${url.context}${res.icon16}" alt="${res.name}"></a></td>
            <td>
               <a href="#" onclick="window.external.openDocument('${relativePath}')" title="Open ${res.name}">${res.name}</a>
            </td>
         <#else>
            <td width="16"><a href="${url.context}${res.url}?ticket=${session.ticket}" target="_blank" title="Open ${res.name}"><img src="${url.context}${res.icon16}" alt="${res.name}"></a></td>
            <td>
               <a href="${url.context}${res.url}?ticket=${session.ticket}" target="_blank">${res.name}</a>
            </td>
         </#if>
      <#else>
            <td width="16"><img src="${url.context}${res.icon16}" alt="${res.name}"></td>
            <td>
               <span>${res.name}</span>
            </td>
      </#if>
         </tr>
   </#list>
      </table>
   </div>

   <div class="taskActionContainer">   
      <span class="taskActions">
   <#list task.transitions as wt>
         <span class="taskAction" onclick="OfficeMyTasks.transitionTask('${task.id}', '${url.context}/command/task/end/${task.id}<#if wt.id?exists>/${wt.id}</#if>', 'Workflow action \'${wt.label?html}\' completed.');">${wt.label?html}</span>
   </#list>
      </span>
      <span class="taskManage">
         <span class="taskAction" onclick="OfficeAddin.openWindowCallback('${url.context}/command/ui/managetask?id=${task.id}&amp;type=${task.qnameType}&amp;container=plain&amp;ticket=${session.ticket}', OfficeMyTasks.refreshPage);">Manage...</span>
      </span>
   </div>
</#if>

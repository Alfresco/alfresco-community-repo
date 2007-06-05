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
            <td width="16"><a href="${url.context}${res.url}" target="new"><img src="${url.context}${res.icon16}" alt="${res.name}"></a></td>
            <td>
      <#if res.isDocument>
               <a href="${url.context}${res.url}" target="_blank">${res.name}</a>
      <#else>
               <span>${res.name}</span>
      </#if>
            </td>
         </tr>
   </#list>
      </table>
   </div>

   <div>   
      <table class="taskActions" style="padding-left:16px">
         <tr>
   <#list task.transitions as wt>
            <td>
               <a class="taskAction" href="#" onclick="OfficeMyTasks.transitionTask('${task.id}', '${url.context}/command/task/end/${task.id}<#if wt.id?exists>/${wt.id}</#if>', 'Workflow action \'${wt.label?html}\' completed.');">${wt.label?html}</a>
            </td>
   </#list>
         </tr>
      </table>
   </div>
</#if>

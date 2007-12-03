<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
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
            <span style="font-weight: bold;">${task.description!""?html}</span>
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
         <#assign relativePath = (res.displayPath?substring(companyhome.name?length+1) + '/' + res.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
         <#if res.name?ends_with(extn) || res.name?ends_with(extnx)>
            <td width="16" valign="top"><a href="${url.context}${res.url}" target="new"><img src="${url.context}${res.icon16}" alt="${res.name}"></a></td>
            <td>
               <a href="#" onclick="window.external.openDocument('${relativePath}')" title="Open ${res.name}">${res.name}</a>
         <#else>
            <td width="16" valign="top"><a href="${url.context}${res.url}?ticket=${session.ticket}" target="_blank" title="Open ${res.name}"><img src="${url.context}${res.icon16}" alt="${res.name}"></a></td>
            <td>
               <a href="${url.context}${res.url}?ticket=${session.ticket}" target="_blank">${res.name}</a>
         </#if>
            <br />
               Modified: ${res.properties.modified?datetime} (${(res.size / 1024)?int}Kb)<br />
         <#if res.isLocked >
               <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="Locked" />
         <#elseif hasAspect(res, "cm:workingcopy") == 1>
               <a href="#" onclick="OfficeMyTasks.runAction('${doc_actions}','checkin','${res.id}');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="Check In" title="Check In" /></a>
         <#else>
               <a href="#" onclick="OfficeMyTasks.runAction('${doc_actions}','checkout','${res.id}');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="Check Out" title="Check Out" /></a>
         </#if>
               <a href="#" onclick="window.external.insertDocument('${relativePath}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="Insert File into Current Document" title="Insert File into Current Document" /></a>
         <#if !res.name?ends_with(".pdf")>
               <a href="#" onclick="OfficeMyTasks.runAction('${doc_actions}','makepdf','${res.id}');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="Make PDF..." title="Make PDF" /></a>
         </#if>
            </td>
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

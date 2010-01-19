<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#if args.t??>
   <#assign taskid = args.t>
   <#if taskid != "">
      <#assign task = workflow.getTaskById(taskid)>
   </#if>
</#if>
<#assign chLen=companyhome.name?length>

<#if task??>
   <table width="260">
      <tr>
         <td width="32" valign="top"><img src="${url.context}/images/office/task_item.gif" alt="${message("office.tip.task_item")}" /></td>
         <td>
            <span style="font-weight: bold;">${(task.description!"")?html?replace("\r", "<br />")}</span>
            <br />     
            <table style="margin-top: 4px;">
               <tr>
                  <td>${message("office.property.status")}:</td>
                  <td>${task.properties["bpm:status"]}</td>
               </tr>
               <tr>
                  <td>${message("office.property.priority")}:</td>
                  <td>${task.properties["bpm:priority"]}</td>
               </tr>
               <tr>
                  <td>${message("office.property.start_date")}:</td>
                  <td>${task.startDate?date}</td>
               </tr>
               <tr>
                  <td>${message("office.property.type")}:</td>
                  <td>${task.type?html}</td>
               </tr>
               <tr>
                  <td>${message("office.property.complete")}:</td>
                  <td>${task.properties["bpm:percentComplete"]}%</td>
               </tr>
            </table>
         </td>
      </tr>
   </table>
   
   <div style="margin-top: 8px; font-weight: bold;">${task.name?html}:</div>
   <div class="taskResources">
      <table width="260">
   <#list task.packageResources as res>
         <tr>
      <#if res.isDocument>
         <#assign relativePath = res.displayPath?substring(chLen + 1) + '/' + res.name />
         <#if res.name?ends_with(extn) || res.name?ends_with(extnx)>
            <td width="16" valign="top"><a href="${url.context}${res.url}" target="_blank"><img src="${url.context}${res.icon16}" alt="${res.name?html}"></a></td>
            <td>
               <a href="#" onclick="ExternalComponent.openDocument('${relativePath}')" title="${message("office.action.open", res.name?html)}"><p class="ellipsis" style="width: 244px;">${res.name?html}</p></a>
         <#else>
            <td width="16" valign="top"><a href="${url.context}${res.url}" target="_blank" title="${message("office.action.open", res.name?html)}"><img src="${url.context}${res.icon16}" alt="${res.name?html}"></a></td>
            <td>
               <a href="${url.context}${res.url}" target="_blank" title="${res.name?html}"><p class="ellipsis" style="width: 244px;">${res.name?html}</p></a>
         </#if>
               ${message("office.property.modified")}: ${res.properties.modified?datetime} (${(res.size / 1024)?int}${message("office.unit.kb")})<br />
         <#if res.isLocked >
               <img src="${url.context}/images/office/lock.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.status.locked")}" />
         <#elseif res.hasAspect("cm:workingcopy")>
               <a href="#" onclick="OfficeMyTasks.runTaskAction('${doc_actions}','checkin','${res.id}');"><img src="${url.context}/images/office/checkin.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkin")}" title="${message("office.action.checkin")}" /></a>
         <#else>
               <a href="#" onclick="OfficeMyTasks.runTaskAction('${doc_actions}','checkout','${res.id}');"><img src="${url.context}/images/office/checkout.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.checkout")}" title="${message("office.action.checkout")}" /></a>
         </#if>
               <a href="#" onclick="ExternalComponent.insertDocument('${relativePath?js_string}', '${res.nodeRef}')"><img src="${url.context}/images/office/insert_document.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.insert")}" title="${message("office.action.insert")}" /></a>
         <#if !res.name?ends_with(".pdf")>
               <a href="#" onclick="OfficeMyTasks.runTaskAction('${doc_actions}','makepdf','${res.id}');"><img src="${url.context}/images/office/makepdf.gif" style="padding:3px 6px 2px 0px;" alt="${message("office.action.transform_pdf")}" title="${message("office.action.transform_pdf")}" /></a>
         </#if>
            </td>
      <#else>
            <td width="16"><img src="${url.context}${res.icon16}" alt="${res.name?html}"></td>
            <td>
               <span>${res.name?html}</span>
            </td>
      </#if>
         </tr>
   </#list>
      </table>
   </div>

   <div class="taskActionContainer">   
      <span class="taskActions">
   <#list task.transitions as wt>
         <span class="taskAction" onclick="OfficeMyTasks.transitionTask('${task.id}', '${url.context}/command/task/end/${task.id}<#if wt.id??>/${wt.id}</#if>', '${message("office.message.workflow_action_complete", wt.label?html)?js_string}');">${wt.label?html}</span>
   </#list>
      </span>
      <span class="taskManage">
         <span class="taskAction" onclick="OfficeAddin.openWindowCallback('${url.context}/command/ui/managetask?id=${task.id}&amp;type=${task.qnameType}&amp;container=plain', OfficeMyTasks.refreshPage);">${message("office.action.manage_task")}...</span>
      </span>
   </div>
</#if>
